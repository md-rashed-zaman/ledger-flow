package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/md-rashed-zaman/ledger-flow/payment-gateway/internal/event"
	"github.com/md-rashed-zaman/ledger-flow/payment-gateway/internal/model"
	"go.uber.org/zap"
)

type TransferHandler struct {
	producer event.Producer
	logger   *zap.Logger
}

func NewTransferHandler(producer event.Producer, logger *zap.Logger) *TransferHandler {
	return &TransferHandler{
		producer: producer,
		logger:   logger,
	}
}

func (h *TransferHandler) CreateTransfer(c *gin.Context) {
	var req model.TransferRequest

	// 1. Bind and Validate JSON
	if err := c.ShouldBindJSON(&req); err != nil {
		h.logger.Warn("Invalid request payload", zap.Error(err))
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// 2. Send to Kafka (Async processing)
	// We use the IdempotencyKey as the Kafka Key to ensure ordering if needed
	err := h.producer.SendTransferEvent(req.IdempotencyKey, req)
	if err != nil {
		h.logger.Error("Failed to publish event", zap.Error(err))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to process request"})
		return
	}

	// 3. Respond immediately (202 Accepted)
	c.JSON(http.StatusAccepted, gin.H{
		"message": "Transfer initiated successfully",
		"status":  "pending",
	})
}
