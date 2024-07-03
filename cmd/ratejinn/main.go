package main

import (
	"fmt"
	"os"

	"github.com/lbihani9/ratejinn/pkg/ratejinn"
	"github.com/redis/go-redis/v9"
)

func main() {
	rdb := redis.NewClient(&redis.Options{
		Addr:     os.Getenv("REDIS_ADDRESS"),
		Username: os.Getenv("REDIS_USERNAME"),
		Password: os.Getenv("REDIS_PASSWORD"),
		DB:       0,
	})

	limiter, _ := ratejinn.NewLimiter(&ratejinn.Options{
		RedisClient: rdb,
		ConfigFile:  "jinn.conf.json",
	})

	fmt.Println("New ratejinn client created", limiter)
}
