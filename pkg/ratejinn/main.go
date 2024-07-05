package ratejinn

import (
	"errors"
	"log"

	"github.com/lbihani9/ratejinn/internal/config"
	"github.com/redis/go-redis/v9"
)

type Options struct {
	RedisClient *redis.Client
	ConfigFile  string
}

type RateJinn struct {
}

func NewLimiter(opt *Options) (*RateJinn, error) {
	if opt.RedisClient == nil {
		log.Println("RedisClient is mandatory option to setup rate jinn.")
		return nil, errors.New("redis client is required to setup rate jinn")
	}

	cfg, err := config.Load(opt.ConfigFile)
	if err != nil {
		return nil, err
	}

	_, treeError := cfg.BuildTree()
	if treeError != nil {
		return nil, treeError
	}

	rj := RateJinn{}

	return &rj, nil
}
