package model

type TransferRequest struct {
	IdempotencyKey string  `json:"idempotency_key" binding:"required"` // Unique ID to prevent double spending
	SourceAccount  string  `json:"source_account" binding:"required"`
	TargetAccount  string  `json:"target_account" binding:"required"`
	Amount         float64 `json:"amount" binding:"required,gt=0"`    // Must be greater than 0
	Currency       string  `json:"currency" binding:"required,len=3"` // e.g., USD
}
