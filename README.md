
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

   - `POST /order/place`
      - Places a new order and checks warehouse stock.
      - **Request Body:** `Order` object (including product ID, quantity, etc.)
      - **Response:** Success or error message indicating order status.

   - `GET /order/all`
      - Fetches a list of all orders.
      - **Response:** List of `Order` objects.

   - `PUT /order/updateStatus/{orderId}`
      - Updates the status of a specific order.
      - **Request Body:** Updated `Order` object (status)
      - **Path Variable:** `orderId` (Long)
      - **Response:** Success or error message.

   - `PUT /order/cancel/{orderId}`
      - Cancels an order if it hasn’t been paid and restores stock levels.
      - **Path Variable:** `orderId` (Long)
      - **Response:** Success or error message.

- **Store Messaging Queue**

   - `POST /api/store/sendEmail`
      - Sends an email through the EmailService.
      - **Request Body:** `EmailDataDTO` (fromEmail, toEmail, subject, body)
      - **Response:** `EmailDataDTO`

   - `POST /api/store/requestTransfer`
      - Initiates a transfer request to the Bank for an order.
      - **Request Body:** `TransferRequestDTO` (orderID, amount, status)
      - **Response:** `TransferRequestDTO`

   - `POST /api/store/delivery`
      - Creates a delivery request for DeliveryCo.
      - **Request Body:** `DeliveryRequestDTO` (orderID, status)
      - **Response:** `DeliveryRequestDTO`

   - `DELETE /api/store/order`
      - Cancels an order and notifies relevant services.
      - **Request Body:** `DeliveryRequestDTO`
      - **Response:** `DeliveryRequestDTO`

- **SSE (Server-Sent Events)**

   - `GET /sse/subscribe`
      - Subscribes to server-sent events for real-time updates on order status changes.
      - **Response:** SSE stream of notifications.

### Bank API

- **Transaction Management**

   - `GET /api/bank/transactions`
      - Retrieves all transactions in the database.
      - **Response:** List of `TransactionData`

   - `PUT /api/bank/transaction/approve/{id}`
      - Approves a transaction and notifies the Store.
      - **Path Variable:** `id` (Long)
      - **Response:** Updated `TransactionData` or 404 if not found.

   - `PUT /api/bank/transaction/decline/{id}`
      - Declines a transaction and notifies the Store.
      - **Path Variable:** `id` (Long)
      - **Response:** Updated `TransactionData` or 404 if not found.

   - `PUT /api/bank/transaction/refund/{orderId}`
      - Processes a refund for a specific order and notifies the Store.
      - **Path Variable:** `orderId` (Long)
      - **Response:** Updated `TransactionData` or 404 if not found.

   - `DELETE /api/bank/clearDB`
      - Clears all data from the Bank database.

### DeliveryCo API

- **Delivery Management**

   - `GET /api/deliveryco/delivery`
      - Retrieves all delivery records.
      - **Response:** List of `DeliveryData`

   - `PUT /api/deliveryco/delivery/receive/{id}`
      - Marks a delivery as received and notifies the Store.
      - **Path Variable:** `id` (Long)
      - **Response:** Updated `DeliveryData` or 404 if not found.

   - `PUT /api/deliveryco/delivery/collected/{id}`
      - Marks a delivery as collected and notifies the Store.
      - **Path Variable:** `id` (Long)
      - **Response:** Updated `DeliveryData` or 404 if not found.

   - `PUT /api/deliveryco/delivery/delivering/{id}`
      - Updates a delivery status to "delivering" and notifies the Store.
      - **Path Variable:** `id` (Long)
      - **Response:** Updated `DeliveryData` or 404 if not found.

   - `PUT /api/deliveryco/delivery/delivered/{id}`
      - Marks a delivery as completed and notifies the Store.
      - **Path Variable:** `id` (Long)
      - **Response:** Updated `DeliveryData` or 404 if not found.

   - `PUT /api/deliveryco/delivery/fail/{id}`
      - Marks a delivery as failed and notifies the Store.
      - **Path Variable:** `id` (Long)
      - **Response:** Updated `DeliveryData` or 404 if not found.

   - `PUT /api/deliveryco/delivery/cancel/{id}`
      - Cancels a delivery and notifies the Store.
      - **Path Variable:** `id` (Long)
      - **Response:** Updated `DeliveryData` or 404 if not found.

   - `DELETE /api/deliveryco/clearDB`
      - Clears all data from the DeliveryCo database.

### EmailService API

- **Email Management**

   - `DELETE /api/email/clearDB`
      - Clears all data from the EmailService database.

### Authentication API (Store)

- **User Authentication**

   - `POST /auth/register`
      - Registers a new user.
      - **Request Body:** `User` object (username, password)
      - **Response:** Success or error message.

   - `POST /auth/login`
      - Logs in a user with username and password.
      - **Request Body:** `User` object (username, password)
      - **Response:** Success or error message.

   - `GET /auth/test`
      - Test endpoint to check if the backend is operational.
      - **Response:** `"Backend is working!"`
