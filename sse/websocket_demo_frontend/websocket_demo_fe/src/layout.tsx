import '@ant-design/v5-patch-for-react-19';
import {useEffect, useState} from "react";
import {Outlet} from "react-router-dom";
import {Spin, List, Typography, Card} from "antd";
import {useCurrentApp} from "./components/context/app.context.tsx";
import {fetchAccountAPI} from "./services/api.ts";
import AppHeader from "./components/layout/app.header.tsx";
import {connectWS, disconnectWS} from "./services/websocket.ts";

function Layout() {
    const {setUser, setIsAuthenticated, isAppLoading, setIsAppLoading, user} = useCurrentApp();
    const [wsMessages, setWsMessages] = useState<string[]>([]);

    useEffect(() => {
        const fetchAccount = async () => {
            const res = await fetchAccountAPI();
            if (res.data) {
                setUser(res.data.user);
                setIsAuthenticated(true);

                // Connect WebSocket after login
                const token = localStorage.getItem('access_token') || '';
                connectWS(token, (payload) => {
                    console.log('WebSocket message:', payload);
                    setWsMessages(prev => [...prev, JSON.stringify(payload)]);
                });
            }
            setIsAppLoading(false);
        };
        fetchAccount();

        return () => {
            disconnectWS();
        };
    }, []);

    return (
        <>
            {!isAppLoading ? (
                <div style={{minHeight: '100vh', display: 'flex', flexDirection: 'column'}}>
                    <AppHeader />
                    <div style={{flex: 1, padding: '20px'}}>
                        {/* Always show a status */}
                        <Card title="WebSocket Status" style={{marginBottom: '20px'}}>
                            <p>Authenticated: {user?.id ? 'Yes' : 'No'}</p>
                            <p>Messages: {wsMessages.length}</p>
                        </Card>
                        {/* Display WebSocket messages only if authenticated */}
                        {user?.id && (
                            <Card title="WebSocket Messages" style={{marginBottom: '20px'}}>
                                <List
                                    size="small"
                                    bordered
                                    dataSource={wsMessages}
                                    renderItem={(msg, idx) => (
                                        <List.Item key={idx}>
                                            <Typography.Text code>{msg}</Typography.Text>
                                        </List.Item>
                                    )}
                                    locale={{emptyText: 'No messages yet'}}
                                    style={{maxHeight: '300px', overflowY: 'auto'}}
                                />
                            </Card>
                        )}
                        <Outlet />
                    </div>
                </div>
            ) : (
                <div style={{
                    position: "fixed",
                    top: "50%",
                    left: "50%",
                    transform: "translate(-50%, -50%)"
                }}>
                    <Spin />
                </div>
            )}
        </>
    );
}

export default Layout;