# SƠ ĐỒ TỔNG QUAN HỆ THỐNG CINEMA MANAGEMENT

Tài liệu mô tả kiến trúc tổng quan hệ thống quản lý rạp chiếu phim theo mô hình Microservices.

---

## 🎬 TỔNG QUAN HỆ THỐNG

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT APPLICATIONS                             │
│                    (Web Browser / Mobile App / Postman)                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ HTTP/HTTPS
                                      ▼
              ┌───────────────────────────────────────────────┐
              │         API GATEWAY (Spring Cloud Gateway)     │
              │                 Port: 8888                     │
              │  • Routing • Load Balancing • Rate Limiting    │
              └───────────────────────────────────────────────┘
                                      │
                                      │ Register & Health Check
                                      ▼
              ┌───────────────────────────────────────────────┐
              │       EUREKA SERVER (Service Discovery)        │
              │                 Port: 8761                     │
              │         • Service Registry                     │
              └───────────────────────────────────────────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        │                             │                             │
        │        All services register and discover each other      │
        │                             │                             │
        └─────────────────────────────┼─────────────────────────────┘
                                      │
     ┌──────────────┬─────────────────┼─────────────────┬──────────────┐
     │              │                 │                 │              │
     ▼              ▼                 ▼                 ▼              ▼
╔═══════════╗ ╔═══════════╗ ╔═══════════╗ ╔═══════════╗ ╔═══════════╗
║   USER    ║ ║  CINEMA   ║ ║ INVENTORY ║ ║  BOOKING  ║ ║NOTIFICATION║
║  SERVICE  ║ ║  SERVICE  ║ ║  SERVICE  ║ ║  SERVICE  ║ ║  SERVICE  ║
╚═══════════╝ ╚═══════════╝ ╚═══════════╝ ╚═══════════╝ ╚═══════════╝
 Port: 8081    Port: 8084    Port: 8083    Port: 8085    Port: 8082

       │                │                │                │                │
       │                │                │                │                │
       ▼                ▼                ▼                ▼                ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│   MySQL     │ │   MySQL     │ │   MySQL     │ │   MySQL     │ │   MySQL     │
│             │ │             │ │             │ │             │ │             │
│   userdb    │ │  cinemadb   │ │ inventorydb │ │  bookingdb  │ │notification │
│             │ │             │ │             │ │             │ │     db      │
│ Port: 3311  │ │ Port: 3308  │ │ Port: 3309  │ │ Port: 3310  │ │ Port: 3312  │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
```

---

## 🔗 GIAO TIẾP GIỮA CÁC SERVICES

### 1. Synchronous Communication (REST API)

```
         REST API Calls (Synchronous)
         ═══════════════════════════

    ┌──────────────┐
    │   BOOKING    │────RestTemplate──────────────► ┌──────────────┐
    │   SERVICE    │  (Reserve/Release Seats)       │   CINEMA     │
    └──────────────┘  Circuit Breaker Pattern       │   SERVICE    │
           │                                         └──────────────┘
           │ RestTemplate
           │ (Check Stock)
           ▼
    ┌──────────────┐
    │  INVENTORY   │
    │   SERVICE    │
    └──────────────┘
           │
           │ All services call
           ▼
    ┌──────────────┐
    │     USER     │
    │   SERVICE    │
    └──────────────┘
        (Validate JWT Token)
```

### 2. Asynchronous Communication (RabbitMQ)

```
         Event-Driven (Asynchronous)
         ═══════════════════════════

                                               ┌───────────────┐
                                               │   RabbitMQ    │
                                               │   Message     │
                                               │   Broker      │
                                               │               │
                                               │ Port: 5672    │
                                               │ UI: 15672     │
                                               └───────────────┘
                                                   │       ▲
                                                   │       │
    ┌──────────────┐                          Publish   Consume
    │   BOOKING    │                               │       │
    │   SERVICE    │───────────────────────────────┘       │
    └──────────────┘                                       │
           │                                               │
           │ Publish Events:                               │
           │ • booking.created                             │
           │ • booking.confirmed                           │
           │ • booking.cancelled                           │
           │                                               │
           └───────────────────────────────────────────────┤
                                                           │
                     ┌─────────────────────────────────────┤
                     │                                     │
                     ▼                                     ▼
          ┌──────────────────┐                  ┌──────────────────┐
          │   INVENTORY      │                  │   NOTIFICATION   │
          │   SERVICE        │                  │   SERVICE        │
          │                  │                  │                  │
          │ • Deduct Stock   │                  │ • Send Email     │
          │ • Restore Stock  │                  │ • Save Record    │
          └──────────────────┘                  └──────────────────┘
                                                         │
                                                         ▼
                                                  ┌──────────────┐
                                                  │ SMTP Server  │
                                                  │ (Gmail, etc) │
                                                  └──────────────┘
```
