package config

import (
	"encoding/json"
	"errors"
	"fmt"
)

type Algorithm struct {
	Type   string      `json:"type"`
	Params interface{} `json:"params"`
}

type TokenBucketAlgorithmParams struct {
	BucketCapacity uint  `json:"bucketCapacity"`
	WindowLength   uint8 `json:"windowLength"`
	Refill         uint  `json:"refill"`
}

type LeakyBucketAlgorithmParams struct {
	BucketCapacity uint `json:"bucketCapacity"`
	Outflow        uint `json:"outflow"`
}

func (a *Algorithm) UnmarshalJSON(data []byte) error {
	var temp struct {
		Type   string          `json:"type"`
		Params json.RawMessage `json:"params"`
	}

	if err := json.Unmarshal(data, &temp); err != nil {
		return err
	}

	a.Type = temp.Type

	switch a.Type {
	case "token-bucket":
		var params TokenBucketAlgorithmParams
		if err := json.Unmarshal(temp.Params, &params); err != nil {
			return err
		}
		a.Params = params
	case "leaky-bucket":
		var params LeakyBucketAlgorithmParams
		if err := json.Unmarshal(temp.Params, &params); err != nil {
			return err
		}
		a.Params = params
	default:
		return fmt.Errorf("unsupported algorithm type: %s", a.Type)
	}

	return nil
}

func (a *Algorithm) ValidateParams() error {
	switch a.Type {
	case "token-bucket":
		p, ok := a.Params.(TokenBucketAlgorithmParams)
		if !ok {
			return errors.New("invalid token bucket params")
		}

		if p.Refill >= p.BucketCapacity {
			return errors.New("refill must be less than bucket capacity")
		}

		if p.BucketCapacity == 0 {
			return errors.New("bucket capacity must be greater than zero")
		}
	case "leaky-bucket":
		p, ok := a.Params.(LeakyBucketAlgorithmParams)
		if !ok {
			return errors.New("invalid leaky bucket params")
		}

		if p.Outflow > p.BucketCapacity {
			return errors.New("outflow must be less than bucket capacity")
		}

		if p.BucketCapacity == 0 {
			return errors.New("bucket capacity must be greater than zero")
		}
	default:
		return errors.New("unsupported algorithm type")
	}

	return nil
}
