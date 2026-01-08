package event

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/IBM/sarama"
	"go.uber.org/zap"
)

type Producer interface {
	SendTransferEvent(key string, payload interface{}) error
	Close() error
}

type KafkaProducer struct {
	producer sarama.SyncProducer
	topic    string
	logger   *zap.Logger
}

func NewKafkaProducer(brokers []string, topic string, logger *zap.Logger) (Producer, error) {
	config := sarama.NewConfig()
	config.Producer.Return.Successes = true
	config.Producer.RequiredAcks = sarama.WaitForAll
	config.Producer.Retry.Max = 5

	var producer sarama.SyncProducer
	var err error

	// --- INDUSTRY GRADE RETRY LOGIC ---
	maxRetries := 15
	for i := 0; i < maxRetries; i++ {
		producer, err = sarama.NewSyncProducer(brokers, config)
		if err == nil {
			logger.Info("Successfully connected to Kafka")
			break
		}

		logger.Warn("Failed to connect to Kafka. Retrying in 2 seconds...",
			zap.Int("attempt", i+1),
			zap.Error(err))

		time.Sleep(2 * time.Second)
	}
	// ----------------------------------

	if err != nil {
		return nil, fmt.Errorf("failed to connect to kafka after retries: %w", err)
	}

	return &KafkaProducer{
		producer: producer,
		topic:    topic,
		logger:   logger,
	}, nil
}

func (k *KafkaProducer) SendTransferEvent(key string, payload interface{}) error {
	bytes, err := json.Marshal(payload)
	if err != nil {
		return fmt.Errorf("failed to marshal payload: %w", err)
	}

	msg := &sarama.ProducerMessage{
		Topic: k.topic,
		Key:   sarama.StringEncoder(key), // Ensures ordering for specific accounts if needed
		Value: sarama.ByteEncoder(bytes),
	}

	partition, offset, err := k.producer.SendMessage(msg)
	if err != nil {
		k.logger.Error("Failed to send message to Kafka", zap.Error(err))
		return err
	}

	k.logger.Info("Message sent to Kafka",
		zap.String("topic", k.topic),
		zap.Int32("partition", partition),
		zap.Int64("offset", offset))

	return nil
}

func (k *KafkaProducer) Close() error {
	return k.producer.Close()
}
