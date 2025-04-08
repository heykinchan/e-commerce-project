
# E-commerce Project

## Overview

This application is a comprehensive system designed to handle order processing, payment, and delivery management across multiple components, each representing a distinct service: `Store`, `Bank`, `DeliveryCo`, and `EmailService`. The system includes fault tolerance mechanisms, asynchronous messaging with message queues, server-sent events (SSE) for real-time updates, and a React frontend for user interaction.

## System Architecture

The system comprises the following core services:

1. **Store**  
   Handles order creation, communication with payment and delivery services, and frontend logic.
   
2. **Bank**  
   Processes payment transactions and issues approvals or declines.

3. **DeliveryCo**  
   Simulates the delivery process with randomised success/failure logic and status updates.

4. **EmailService**  
   Sends email notifications about order and delivery status to customers.

Each service runs independently, owns its own PostgreSQL database, and communicates asynchronously using REST APIs, custom message queues, and SSE.


## Components

| Component       | Port             | Description                              |
|----------------|------------------|------------------------------------------|
| Frontend        | `localhost:3000` | React app for placing & tracking orders  |
| Store Service   | `localhost:8083` | Handles orders, SSE, and inter-service coordination |
| Bank Service    | `localhost:8084` | Manages transactions and payment status  |
| DeliveryCo      | `localhost:8085` | Handles delivery simulation and updates  |
| EmailService    | `localhost:8086` | Sends notification emails to customers   |

These components communicate via RESTful APIs, message queues, and SSE for real-time updates.

## Setup and Installation

### Prerequisites

- Java 17+
- Node.js & npm
- PostgreSQL
- IntelliJ (recommended IDE)
- PgAdmin (recommended DB tool)

### Installation Steps

1. **IDE and Database Management Tool Set-up**:
   - Suggest using IntelliJ and PgAdmin to run the program for better installation

2.**Backend Services Setup**:
   - Start the PgAdmin application, and create a database call with the following set-up:
   ```bash
   Database port: 5432
   Database name: comp5348gp
   Database username: postgres
   Database password: 123456
   ```
   - In this Database, run the create_mq_table.sql PostgreSQL script in the following directory to initiate the Message Queue
     ```bash
     ./Source Code/create_mq_table.sql
     ```
   - In this Database, run the intert_warehouse_stock.sql PostgreSQL script int he folloing directory to insert the stocks to the warehouse database
   ```bash
     ./Source Code/intert_warehouse_stock.sql
   ```
   
3.**Frontend Setup**:
   - Navigate to the `frontend` directory.
   - Install dependencies and start the React application:
     ```bash
     npm install
     npm start
     ```

## Running the Application

1. **Start the Backend Services**:
   - Run each of the applications of `Store`, `Bank`, `DeliveryCo`, `EmailService` in IntelliJ to start the system

2. **Start the Frontend**:
   - From the `frontend` directory, run:
     ```bash
     npm start
     ```
   - Access the frontend at `http://localhost:3000`.

## Features

### Store Service
- **User Login**: Allows customer login.
- **Order Management**: Place new orders, cancel orders, and check order status.
- **Payment and Delivery Requests**: Sends requests to the Bank and DeliveryCo for payment and delivery processing.
- **Real-time Updates**: Uses SSE to push updates from the backend to the frontend.

### Bank Service
- **Payment Processing**: Handles payment requests and updates order status.

### DeliveryCo Service
- **Delivery Management**: Processes delivery requests with a randomised 95% success rate.

### EmailService
- **Notification**: Sends email notifications to customers about their order and delivery status.

## API Documentation

### Store API


- **Order Management**
  
| Method | Endpoint                        | Request Body                              | Response                      | Description                           |
|--------|----------------------------------|-------------------------------------------|-------------------------------|---------------------------------------|
| POST   | `/order/place`                  | `Order` (productId, quantity, etc.)       | Success or error message      | Places a new order and checks stock  |
| GET    | `/order/all`                    | –                                         | List of `Order` objects       | Fetches all orders                   |
| PUT    | `/order/updateStatus/{orderId}` | `Order` (status)                          | Success or error message      | Updates the status of an order       |
| PUT    | `/order/cancel/{orderId}`       | –                                         | Success or error message      | Cancels unpaid order and restocks    |

- **Store Messaging Queue**
  
| Method | Endpoint                        | Request Body                                     | Response             | Description                             |
|--------|----------------------------------|--------------------------------------------------|----------------------|-----------------------------------------|
| POST   | `/api/store/sendEmail`          | `EmailDataDTO` (fromEmail, toEmail, subject, body) | `EmailDataDTO`    | Sends email via EmailService           |
| POST   | `/api/store/requestTransfer`    | `TransferRequestDTO` (orderId, amount, status)     | `TransferRequestDTO` | Initiates transfer request to Bank     |
| POST   | `/api/store/delivery`           | `DeliveryRequestDTO` (orderId, status)            | `DeliveryRequestDTO` | Creates a delivery request             |
| DELETE | `/api/store/order`              | `DeliveryRequestDTO`                              | `DeliveryRequestDTO` | Cancels order and notifies services    |


- **SSE (Server-Sent Events)**
  
| Method | Endpoint          | Request Body | Response     | Description                        |
|--------|-------------------|--------------|--------------|------------------------------------|
| GET    | `/sse/subscribe`  | –            | SSE stream   | Subscribes to real-time updates    |


### Bank API

- **Transaction Management**
  
| Method | Endpoint                                      | Request Body | Response                      | Description                        |
|--------|-----------------------------------------------|--------------|-------------------------------|------------------------------------|
| GET    | `/api/bank/transactions`                      | –            | List of `TransactionData`     | Gets all transactions              |
| PUT    | `/api/bank/transaction/approve/{id}`          | –            | Updated `TransactionData` or 404 | Approves transaction            |
| PUT    | `/api/bank/transaction/decline/{id}`          | –            | Updated `TransactionData` or 404 | Declines transaction            |
| PUT    | `/api/bank/transaction/refund/{orderId}`      | –            | Updated `TransactionData` or 404 | Refunds a transaction           |
| DELETE | `/api/bank/clearDB`                           | –            | Success message               | Clears all Bank DB records         |


### DeliveryCo API

- **Delivery Management**
  
| Method | Endpoint                                         | Request Body | Response                    | Description                       |
|--------|--------------------------------------------------|--------------|-----------------------------|------------------------------------|
| GET    | `/api/deliveryco/delivery`                       | –            | List of `DeliveryData`      | Retrieves all delivery records    |
| PUT    | `/api/deliveryco/delivery/receive/{id}`          | –            | Updated `DeliveryData` or 404 | Marks as received              |
| PUT    | `/api/deliveryco/delivery/collected/{id}`        | –            | Updated `DeliveryData` or 404 | Marks as collected             |
| PUT    | `/api/deliveryco/delivery/delivering/{id}`       | –            | Updated `DeliveryData` or 404 | Marks as delivering            |
| PUT    | `/api/deliveryco/delivery/delivered/{id}`        | –            | Updated `DeliveryData` or 404 | Marks as delivered             |
| PUT    | `/api/deliveryco/delivery/fail/{id}`             | –            | Updated `DeliveryData` or 404 | Marks as failed                |
| PUT    | `/api/deliveryco/delivery/cancel/{id}`           | –            | Updated `DeliveryData` or 404 | Cancels delivery                |
| DELETE | `/api/deliveryco/clearDB`                        | –            | Success message             | Clears all delivery records       |

### EmailService API

- **Email Management**
  
| Method | Endpoint             | Request Body | Response        | Description                     |
|--------|----------------------|--------------|-----------------|---------------------------------|
| DELETE | `/api/email/clearDB` | –            | Success message | Clears all email DB records     |


### Authentication API (Store)

- **User Authentication**
  
| Method | Endpoint         | Request Body                        | Response                  | Description                     |
|--------|------------------|-------------------------------------|---------------------------|---------------------------------|
| POST   | `/auth/register` | `User` (username, password)         | Success or error message  | Registers a new user            |
| POST   | `/auth/login`    | `User` (username, password)         | Success or error message  | Logs in a user                  |
| GET    | `/auth/test`     | –                                   | "Backend is working!"     | Health check endpoint           |

