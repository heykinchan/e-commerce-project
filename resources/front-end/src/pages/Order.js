// src/pages/Order.js
import React, { useState, useEffect } from 'react';
import { Form, InputNumber, Button, Card, notification, Spin, Table, Input, Popconfirm } from 'antd';
import { useLocation } from 'react-router-dom'; 
import axios from 'axios';

const Order = () => {
  const { state } = useLocation(); 
  const [username, setUsername] = useState('');
  const [showOrderList, setShowOrderList] = useState(false); 
  const [loading, setLoading] = useState(false); 
  const [orders, setOrders] = useState([]); 
  const [orderLoading, setOrderLoading] = useState(false); 
  const [totalAmount, setTotalAmount] = useState(100.5); 

  useEffect(() => {
    if (state && state.username) {
      setUsername(state.username);
    } else {
      notification.error({
        message: 'Error',
        description: 'Username not found. Please login first.',
      });
    }
  }, [state]);

  useEffect(() => {
    // Fetch orders on component mount
    fetchOrders();

    // // Initialize the SSE connection
    // const eventSource = new EventSource('http://localhost:8083/sse/subscribe');
    //
    // eventSource.onmessage = (event) => {
    //   notification.info({
    //     message: 'Order Update',
    //     description: event.data,
    //   });
    //
    //   // Refetch orders when an update is received
    //   fetchOrders();
    // };

    // Cleanup the SSE connection on unmount
    return () => {
      // eventSource.close();
    };
  }, []);

  const fetchOrders = () => {
    setOrderLoading(true);
    axios
      .get('http://localhost:8083/order/all')
      .then((response) => {
        const updatedOrders = response.data.map((order) => {
          
          if ((order.transactionStatus === 'successful') && (order.deliveryStatus === 'completed' || order.deliveryStatus === 'failed')&&
          order.status !== 'FINISHED') {
            axios.put(`http://localhost:8083/order/updateStatus/${order.orderId}`, { status: 'FINISHED' })
            .then(() => {
              notification.success({
                message: 'Order Status Updated',
                description: `Order ${order.orderId} status updated to FINISHED.`,
              });
            })
            .catch(() => {
              notification.error({
                message: 'Update Failed',
                description: `Failed to update status for order ${order.orderId}.`,
              });
            });
            return { ...order, status: 'FINISHED' };
          }

          if ((order.transactionStatus === 'declined') &&
          order.status !== 'FINISHED') {
            axios.put(`http://localhost:8083/order/updateStatus/${order.orderId}`, { status: 'FINISHED' })
            .then(() => {
              notification.success({
                message: 'Order Status Updated',
                description: `Order ${order.orderId} status updated to FINISHED.`,
              });
            })
            .catch(() => {
              notification.error({
                message: 'Update Failed',
                description: `Failed to update status for order ${order.orderId}.`,
              });
            });
            return { ...order, status: 'FINISHED' };
          }
          
          return order;
        });
  
        setOrders(updatedOrders);
        setOrderLoading(false);
      })
      .catch((error) => {
        notification.error({
          message: 'Error',
          description: 'Failed to fetch orders.',
        });
        setOrderLoading(false);
      });
  };

  const handleQuantityChange = (quantity) => {
    setTotalAmount(100.5 * quantity);
  };

  const onFinish = (values) => {
    setLoading(true);
    const orderData = {
      customerUsername: username,
      totalAmount: totalAmount,
      productId: 101,
      quantity: values.quantity,
    };

    axios
      .post('http://localhost:8083/order/place', orderData)
      .then((response) => {
        notification.success({
          message: 'Order Placed',
          description: response.data,
        });
        setLoading(false);
      })
      .catch((error) => {
        notification.error({
          message: 'Order Failed',
          description: error.response?.data || 'An error occurred',
        });
        setLoading(false);
      });
  };

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

  const columns = [
    {
      title: 'Order ID',
      dataIndex: 'orderId',
      key: 'orderId',
    },
    {
      title: 'Product',
      dataIndex: 'productId',
      key: 'productId',
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
  render: (_, record) => {
    const restrictedStatuses = [
      'received',
      'collected',
      'delivering',
      'completed',
      'failed',
      'toCancel',
      'cancelled',
    ];

    return (
      <>
        {!restrictedStatuses.includes(record.deliveryStatus?.toLowerCase()) && 
          record.status === 'PENDING' && record.transactionStatus !== 'declined' &&(
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
    );
  },
}

  ];
  

  return (
    <div style={{ padding: '50px' }}>
      <h1>Order Page</h1>
      <div style={{ marginBottom: '20px' }}>
        <Button
          type="primary"
          onClick={() => {
            setShowOrderList(!showOrderList);
            if (!showOrderList) fetchOrders();
          }}
        >
          {showOrderList ? 'Hide Order List' : 'Show Order List'}
        </Button>
        <Button type="default" onClick={fetchOrders}>
          Refresh
        </Button>
      </div>

      {showOrderList ? (
        <Spin spinning={orderLoading}>
          <Table columns={columns} dataSource={orders} rowKey="orderId" />
        </Spin>
      ) : (
        <Card title="Place Order" style={{ width: 400 }}>
          <Form layout="vertical" onFinish={onFinish} initialValues={{ quantity: 1}}>
            <Form.Item label="Product">
              <Input value="Product1" disabled />
            </Form.Item>

            <Form.Item
              label="Quantity"
              name="quantity"
              rules={[{ required: true, message: 'Please enter quantity!' }]}
            >
              <InputNumber min={1} style={{ width: '100%' }} onChange={handleQuantityChange} />
            </Form.Item>

            <Form.Item label="Total Amount">
              <InputNumber value={totalAmount} disabled style={{ width: '100%' }} />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" block loading={loading}>
                Place Order
              </Button>
            </Form.Item>
          </Form>
        </Card>
      )}
    </div>
  );
};

export default Order;
