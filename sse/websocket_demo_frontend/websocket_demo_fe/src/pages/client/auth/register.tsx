import {Button, Form, Input, Typography, Row, Col, Card, App} from 'antd';
import type { FormProps } from 'antd';
import { Link, useNavigate } from 'react-router-dom';
import {registerAPI} from "../../../services/api.ts";
import { useState } from 'react';

type FieldType = {
    fullName: string;
    username: string;
    email: string;
    password: string;
};

const RegisterPage = () => {
    const navigate = useNavigate();
    const [isSubmit, setIsSubmit] = useState(false);
    const {message} = App.useApp();
    const onFinish: FormProps<FieldType>['onFinish'] = async (values) => {
        setIsSubmit(true);
        const {fullName, username, email, password} = values;
        const res = await registerAPI(fullName, email, password, username);
        if (res?.data) {
            message.success("Register user successfully!");
            navigate("/login");
        } else {
            message.error(res.message);
        }
        setIsSubmit(false);
    };

    const onFinishFailed: FormProps<FieldType>['onFinishFailed'] = (errorInfo) => {
        console.log('Failed:', errorInfo);
    };

    return (
        <Row
            justify="center"
            align="middle"
            style={{ minHeight: '100vh', padding: '24px' }}
        >
            <Col xs={24} sm={18} md={12} lg={8}>
                <Card>
                    <Typography.Title level={2} style={{ textAlign: 'center' }}>
                        Register
                    </Typography.Title>

                    <Form
                        name="register-form"
                        labelCol={{ span: 8 }}
                        wrapperCol={{ span: 16 }}
                        initialValues={{ remember: true }}
                        onFinish={onFinish}
                        onFinishFailed={onFinishFailed}
                        autoComplete="off"
                    >
                        <Form.Item<FieldType>
                            label="Username"
                            name="username"
                            rules={[{ required: true, message: 'Please input your username!' }]}
                        >
                            <Input />
                        </Form.Item>

                        <Form.Item<FieldType>
                            label="Full Name"
                            name="fullName"
                            rules={[{ required: true, message: 'Please input your full name!' }]}
                        >
                            <Input />
                        </Form.Item>

                        <Form.Item<FieldType>
                            label="Email"
                            name="email"
                            rules={[
                                { required: true, message: 'Please input your email!' },
                                { type: 'email', message: 'Please enter a valid email!' },
                            ]}
                        >
                            <Input />
                        </Form.Item>

                        <Form.Item<FieldType>
                            label="Password"
                            name="password"
                            rules={[{ required: true, message: 'Please input your password!' }]}
                        >
                            <Input.Password />
                        </Form.Item>
                        <Form.Item
                            label="Retype Password"
                            name="retypePassword"
                            dependencies={['password']}
                            rules={[
                                { required: true, message: 'Please retype your password!' },
                                ({ getFieldValue }) => ({
                                    validator(_, value) {
                                        if (!value || getFieldValue('password') === value) {
                                            return Promise.resolve();
                                        }
                                        return Promise.reject(new Error('Passwords do not match!'));
                                    },
                                }),
                            ]}
                            //   wrapperCol={{ span: 16, offset: 8 }}
                        >
                            <Input.Password />
                        </Form.Item>

                        <Form.Item wrapperCol={{ offset: 8, span: 16 }}>
                            <Button type="primary" htmlType="submit" block loading={isSubmit}>
                                Register
                            </Button>
                        </Form.Item>

                        <Form.Item wrapperCol={{ offset: 8, span: 16 }}>
                            <Typography.Text>
                                Already have an account? <Link to="/login">Login</Link>
                            </Typography.Text>
                        </Form.Item>
                    </Form>
                </Card>
            </Col>
        </Row>
    );
};

export default RegisterPage;