import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import Layout from './layout.tsx'
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import LoginPage from "./pages/client/auth/login.tsx";
import RegisterPage from "./pages/client/auth/register.tsx";
import {App, ConfigProvider} from "antd";
import {AppProvider} from "./components/context/app.context.tsx";
import enUS from 'antd/locale/en_US';
import '@ant-design/v5-patch-for-react-19';
import HomePage from './pages/home.tsx';
import SseBackupPage from "./pages/client/backup/sse.backup.page.tsx";

const router = createBrowserRouter([
    {
        path: "/",
        element: <Layout/>,
        children: [
            {
                index: true,
                element: <HomePage/>
            },
            {
                path: "backup-sse",
                element: <SseBackupPage/>
            }
        ]
    },
    {
        path: "/login",
        element: <LoginPage/>
    },
    {
        path: "/register",
        element: <RegisterPage/>
    }
])
createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <App>
            <AppProvider>
                <ConfigProvider locale={enUS}>
                    <RouterProvider router={router} />
                </ConfigProvider>
            </AppProvider>
        </App>
    </StrictMode>,
)
