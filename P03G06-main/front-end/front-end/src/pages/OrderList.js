// src/pages/OrderList.js
import React, { useEffect, useState } from 'react';
import {Table, Button, notification, Spin, Popconfirm} from 'antd';
import axios from 'axios';

const OrderList = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  // Function to fetch all orders
  const fetchOrders = () => {
    axios
        .get('http://localhost:8083/order/all')
        .then((response) => {
          setOrders(response.data);
          setLoading(false);
        })
        .catch((error) => {
          notification.error({
            message: 'Error',
            description: 'Failed to fetch orders.',
          });
          setLoading(false);
        });
  }

  const handleCancel = (orderId) => {
    axios
        .put(`http://localhost:8083/order/cancel/${orderId}`)
        .then(() => {
          notification.success({
            message: 'Order Cancelled',
            description: `Order ${orderId} has been cancelled and stock returned.`,
          });

          setOrders((prevOrders) =>
              prevOrders.map((order) =>
                  order.orderId === orderId ? { ...order, status: 'CANCELLED' } : order
              )
          );
        })
        .catch(() => {
          notification.error({
            message: 'Cancellation Failed',
            description: 'An error occurred during cancellation.',
          });
        });
  };

  // Fetch the orders and subscribe to SSE
  useEffect(() => {
    fetchOrders();

    // Initialize the EventSource for SSE
    const eventSource = new EventSource('http://localhost:8083/sse/subscribe');

    eventSource.onmessage = (event) => {
      notification.info({
        message: 'Order Update',
        description: event.data,
      });

      // Refetch orders to get the latest data
      fetchOrders();
    };

    // Cleanup on component unmount
    return () => {
      eventSource.close();
    };
  }, []);


  const columns = [
    {
      title: 'Order ID',
      dataIndex: 'orderId',
      key: 'orderId',
    },
    {
      title: 'Customer Username',
      dataIndex: 'customerUsername',
      key: 'customerUsername',
    },
    {
      title: 'Total Amount',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: (amount) => `$${amount.toFixed(2)}`,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
    },
    {
      title: 'Transaction Status',
      dataIndex: 'transactionStatus',
      key: 'transactionStatus',
      render: (status) => (
          <span style={{ color: status === 'successful' ? 'green' : 'red' }}>
          {status}
        </span>
      ),
    },
    {
      title: 'Delivery Status',
      dataIndex: 'deliveryStatus',
      key: 'deliveryStatus',
      render: (status) => (
          <span style={{ color: status === 'delivered' ? 'green' : 'orange' }}>
          {status}
        </span>
      ),
    },
    {
      title: 'Action',
      key: 'action',
      render: (_, record) => (
          <>
            {record.status === 'PENDING' && (
                <Popconfirm
                    title="Are you sure you want to cancel this order?"
                    onConfirm={() => handleCancel(record.orderId)}
                    okText="Yes"
                    cancelText="No"
                >
                  <Button type="primary" danger>
                    Cancel
                  </Button>
                </Popconfirm>
            )}
          </>
      ),
    },
  ];

  return (
    <div style={{ padding: '50px' }}>
      <h1>Order List</h1>
      <Spin spinning={loading}>
        <Table columns={columns} dataSource={orders} rowKey="orderId" />
      </Spin>
    </div>
  );
};

export default OrderList;
