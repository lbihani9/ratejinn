package config

import (
	"encoding/json"
	"errors"
	"io"
	"log"
	"os"
	"path/filepath"
	"strings"
)

const (
	defaultConcurrentRequestLimit = 10
	defaultShedThreshold          = 80
)

type RateLimiterDefinition struct {
	Scope       string      `json:"scope"`
	Pattern     string      `json:"pattern"`
	IsPriority  bool        `json:"isPriority"`
	RateLimiter RateLimiter `json:"rateLimiter"`
	Algorithm   Algorithm   `json:"algorithm"`
}

type RateJinnConfig struct {
	TimeUnit                               string                  `json:"timeUnit"`
	EnableLoadShedding                     bool                    `json:"enableLoadShedding"`
	ShedThreshold                          uint8                   `json:"shedThreshold"`
	EnableConcurrentRequestLimit           bool                    `json:"enableConcurrentRequestLimit"`
	ConcurrentRequestIdentificationPattern string                  `json:"concurrentRequestIdentificationPattern"`
	ConcurrentRequestLimit                 uint64                  `json:"concurrentRequestLimit"`
	Definitions                            []RateLimiterDefinition `json:"definitions"`
}

// PathNode represents a node in a hierarchical configuration tree for the RateJinn configuration.
// Each node has a name, an optional embedded JinnNode for storing configuration details, and
// a map of children nodes keyed by their names, allowing for flexible and nested configuration structures.
type PathNode struct {
	Name string
	*JinnNode
	Children map[string]*PathNode
}

// JinnNode is the leaf node containing the configuration for its prefix
type JinnNode struct {
	IsPriority bool
	// TODO: Add further config paramters
}

func Load(path string) (*RateJinnConfig, error) {
	if path == "" {
		log.Println("configuration file name missing. Using 'jinn.conf.json' as default name")
		path = "jinn.conf.json"
	}

	absPath, err := filepath.Abs(path)
	if err != nil {
		return nil, err
	}

	file, err := os.Open(absPath)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	bytes, err := io.ReadAll(file)
	if err != nil {
		return nil, err
	}

	var config RateJinnConfig
	if err := json.Unmarshal(bytes, &config); err != nil {
		return nil, err
	}

	if err := config.validate(); err != nil {
		return nil, err
	}

	return &config, nil
}

func (cfg *RateJinnConfig) validate() error {
	if cfg.TimeUnit == "" {
		return errors.New("time unit is a mandatory field")
	}

	if cfg.EnableLoadShedding {
		if cfg.ShedThreshold > 100 {
			return errors.New("shred threshold value can only be from 1 to 100")
		}

		if cfg.ShedThreshold == 0 {
			cfg.ShedThreshold = defaultShedThreshold
		}
	}

	if cfg.EnableConcurrentRequestLimit {
		if cfg.ConcurrentRequestIdentificationPattern == "" {
			return errors.New("concurrent request identification pattern cannot be empty if concurrent request limit is enabled")
		}

		if cfg.ConcurrentRequestLimit == 0 {
			cfg.ConcurrentRequestLimit = defaultConcurrentRequestLimit
		}
	}

	if len(cfg.Definitions) == 0 {
		return errors.New("at least one definition is required")
	}

	for _, def := range cfg.Definitions {
		// TODO: ensure RateLimiter and Algorithm are not empty

		if err := def.RateLimiter.ValidateParams(); err != nil {
			return err
		}

		if err := def.Algorithm.ValidateParams(); err != nil {
			return err
		}

		if def.Scope != "endpoints" {
			return errors.New("scope must be 'endpoints'")
		}

		if def.Pattern == "" || def.Pattern[0] != '/' {
			return errors.New("pattern is required and it must start with '/'")
		}

		// When "/" pattern is present in configs, other definitions will be ignored.
		if def.Pattern == "/" {
			cfg.Definitions = []RateLimiterDefinition{
				{
					Scope:       def.Scope,
					Pattern:     def.Pattern,
					IsPriority:  def.IsPriority,
					RateLimiter: def.RateLimiter,
					Algorithm:   def.Algorithm,
				},
			}
			break
		}
	}

	return errors.New("invalid ratejinn configurations")
}

func (cfg *RateJinnConfig) BuildTree() (*PathNode, error) {
	root := &PathNode{"/", nil, make(map[string]*PathNode)}

	for _, def := range cfg.Definitions {
		split := strings.Split(def.Pattern, "/")
		ptr := root
		for _, part := range split {
			if part == "" {
				continue
			}
			_, ok := ptr.Children[part]
			if !ok {
				ptr.Children[part] = &PathNode{part, nil, make(map[string]*PathNode)}
			}
			ptr = ptr.Children[part]
		}
		ptr.JinnNode = &JinnNode{def.IsPriority}
		// TODO: Add further config parameters
	}

	return root, nil
}
