package config

import (
	"encoding/json"
	"errors"
	"io"
	"log"
	"os"
	"path/filepath"
)

const (
	defaultConcurrentRequestLimit = 20
	defaultShedThreshold          = 80
)

type AlgorithmParams struct {
	BucketCapacity int `json:"bucketCapacity"`
	WindowLength   int `json:"windowLength"`
	Refill         int `json:"refill"`
}

type Algorithm struct {
	Type   string          `json:"type"`
	Params AlgorithmParams `json:"params"`
}

type RateLimiterParams struct {
	Count        int `json:"count"`
	WindowLength int `json:"windowLength"`
}

type RateLimiter struct {
	Type                      string            `json:"type"`
	Params                    RateLimiterParams `json:"params"`
	EnablePerUser             bool              `json:"enablePerUser"`
	UserIdentificationPattern string            `json:"userIdentificationPattern"`
}

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

func LoadConfig(path string) (*RateJinnConfig, error) {
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

	if err := validateConfig(&config); err != nil {
		return nil, err
	}

	return &config, nil
}

func validateConfig(cfg *RateJinnConfig) error {
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
			return errors.New("concurrent request indentification pattern cannot be empty if concurrent request limit is enable")
		}

		if cfg.ConcurrentRequestLimit == 0 {
			cfg.ConcurrentRequestLimit = defaultConcurrentRequestLimit
		}
	}

	// TODO: Add validation checks for other fields as well.
	return errors.New("invalid jinn configuration")
}
