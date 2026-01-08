package main

import (
	"log"

	"github.com/gin-gonic/gin"
	internalConfig "github.com/md-rashed-zaman/ledger-flow/payment-gateway/internal/config"
	"github.com/md-rashed-zaman/ledger-flow/payment-gateway/internal/event"
	"github.com/md-rashed-zaman/ledger-flow/payment-gateway/internal/handler"
	"go.uber.org/zap"
)

func main() {
	// 1. Initialize Logger
	logger, _ := zap.NewProduction()
	defer logger.Sync()

	// 2. Load Config
	cfg, err := internalConfig.LoadConfig()
	if err != nil {
		logger.Fatal("Failed to load config", zap.Error(err))
	}

	// 3. Initialize Kafka Producer
	producer, err := event.NewKafkaProducer(cfg.Kafka.Brokers, cfg.Kafka.Topic, logger)
	if err != nil {
		logger.Fatal("Failed to connect to Kafka", zap.Error(err))
	}
	defer producer.Close()

	// 4. Initialize Handlers
	transferHandler := handler.NewTransferHandler(producer, logger)

	// 5. Setup Router
	r := gin.Default()
	r.POST("/api/v1/transfer", transferHandler.CreateTransfer)

	// 6. Start Server
	logger.Info("Starting Payment Gateway", zap.String("port", cfg.Server.Port))
	if err := r.Run(cfg.Server.Port); err != nil {
		log.Fatal("Server failed to start", err)
	}
}
