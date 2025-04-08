// src/pages/Login.js
import React, { useState } from 'react';
import { Button, Form, Input, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const { Title } = Typography;

const Login = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const response = await axios.post('http://localhost:8083/auth/login', {
        username: values.username,
        password: values.password,
      });

      if (response.status === 200) {
        message.success('Login successful!');
        navigate('/order',{ state: { username: values.username }}); // Redirect to order page
      }
    } catch (error) {
      if (error.response) {
        message.error('Invalid username or password.');
      } else if (error.request) {
        message.error('Backend server is not available.');
      } else {
        message.error('An unexpected error occurred.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <Form
        name="login"
        onFinish={onFinish}
        layout="vertical"
        style={{ width: 300 }}
      >
        <Title level={2} style={{ textAlign: 'center' }}>Login</Title>
        <Form.Item
          label="Username"
          name="username"
          rules={[{ required: true, message: 'Please enter your username!' }]}
        >
          <Input placeholder="Enter your username" />
        </Form.Item>

        <Form.Item
          label="Password"
          name="password"
          rules={[{ required: true, message: 'Please enter your password!' }]}
        >
          <Input.Password placeholder="Enter your password" />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block>
            Login
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

export default Login;
