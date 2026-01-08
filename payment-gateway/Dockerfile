# Stage 1: Build
# Changed from 1.21 to alpine (latest) to match your local Go version
FROM golang:alpine AS builder 
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN go build -o main cmd/api/main.go

# Stage 2: Run
FROM alpine:latest
WORKDIR /app
COPY --from=builder /app/main .
COPY --from=builder /app/config ./config

EXPOSE 8081
CMD ["./main"]