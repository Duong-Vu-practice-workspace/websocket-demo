import {Button, Form, Input, Typography, Row, Col, Card, App} from 'antd';
import type { FormProps } from 'antd';
import {Link, useNavigate} from 'react-router-dom';
import {loginAPI} from "../../../services/api.ts";
import { useState } from 'react';
import {useCurrentApp} from "../../../components/context/app.context.tsx";

type FieldType = {
    fullName: string;
    username: string;
    email: string;
    password: string;
};

const LoginPage = () => {
    const navigate = useNavigate();
    const [isSubmit, setIsSubmit] = useState(false);
    const {message, notification} = App.useApp();
    const {setIsAuthenticated, setUser} = useCurrentApp();
    const onFinish: FormProps<FieldType>['onFinish'] = async (values) => {
        setIsSubmit(true);
        const {username, password} = values;
        const res = await loginAPI(username, password);
        setIsSubmit(false);
        if (res?.data) {
            localStorage.setItem('access_token', res.data.access_token);
            message.success("Login successfully!");
            setIsAuthenticated(true);
            setUser(res.data.user);
            navigate("/");
        } else {
            notification.error(
                {
                    message: "Login failed!",
                    description: res.message && Array.isArray(res.message) ? res.message[0] : res.message,
                }
            )
        }

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
                        Login
                    </Typography.Title>

                    <Form
                        name="login-form"
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
                            label="Password"
                            name="password"
                            rules={[{ required: true, message: 'Please input your password!' }]}
                        >
                            <Input.Password />
                        </Form.Item>

                        <Form.Item wrapperCol={{ offset: 8, span: 16 }}>
                            <Button type="primary" htmlType="submit" block loading={isSubmit}>
                                Login
                            </Button>
                        </Form.Item>
                        <Form.Item wrapperCol={{ offset: 8, span: 16 }}>
                            <Typography.Text>
                                Don't have account? <Link to="/register">Register</Link>
                            </Typography.Text>
                        </Form.Item>
                    </Form>
                </Card>
            </Col>
        </Row>
    );
};

export default LoginPage;