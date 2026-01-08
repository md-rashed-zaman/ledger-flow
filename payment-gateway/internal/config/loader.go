package config

import (
	"strings"

	"github.com/spf13/viper"
)

type Config struct {
	Server struct {
		Port string
	}
	Kafka struct {
		Brokers []string
		Topic   string
	}
}

func LoadConfig() (*Config, error) {
	viper.AddConfigPath("./config")
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")

	// 1. Read from file first
	if err := viper.ReadInConfig(); err != nil {
		// It's okay if config file is missing if we provide ENV vars
		if _, ok := err.(viper.ConfigFileNotFoundError); !ok {
			return nil, err
		}
	}

	// 2. Allow Environment Variables to override
	// Example: KAFKA_TOPIC will override kafka.topic
	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	viper.AutomaticEnv()

	var cfg Config
	if err := viper.Unmarshal(&cfg); err != nil {
		return nil, err
	}
	return &cfg, nil
}
