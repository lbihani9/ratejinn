package config

import (
	"encoding/json"
	"errors"
	"fmt"
)

type RateLimiter struct {
	Type                      string      `json:"type"`
	Params                    interface{} `json:"params"`
	EnablePerUser             bool        `json:"enablePerUser"`
	UserIdentificationPattern string      `json:"userIdentificationPattern"`
}

type RequestRateLimiterParams struct {
	Count        uint `json:"count"`
	WindowLength uint `json:"windowLength"`
}

func (r *RateLimiter) UnmarshalJSON(data []byte) error {
	var temp struct {
		Type                      string          `json:"type"`
		Params                    json.RawMessage `json:"params"`
		EnablePerUser             bool            `json:"enablePerUser"`
		UserIdentificationPattern string          `json:"userIdentificationPattern"`
	}

	if err := json.Unmarshal(data, &temp); err != nil {
		return err
	}

	r.Type = temp.Type
	r.EnablePerUser = temp.EnablePerUser
	r.UserIdentificationPattern = temp.UserIdentificationPattern

	switch r.Type {
	case "request-rate-limiter":
		var params RequestRateLimiterParams
		if err := json.Unmarshal(temp.Params, &params); err != nil {
			return err
		}
		r.Params = params
	default:
		return fmt.Errorf("unsupported rate limiter type: %s", r.Type)
	}

	return nil
}

func (r *RateLimiter) ValidateParams() error {
	switch r.Type {
	case "request-rate-limiter":
		p, ok := r.Params.(RequestRateLimiterParams)
		if !ok {
			return errors.New("invalid request rate limiter params")
		}

		if p.WindowLength == 0 {
			return errors.New("invalid request rate limiter window length")
		}
	default:
		return errors.New("unsupported rate limiter type")
	}

	if r.EnablePerUser && r.UserIdentificationPattern == "" {
		return errors.New("invalid user identification pattern")
	}

	return nil
}
