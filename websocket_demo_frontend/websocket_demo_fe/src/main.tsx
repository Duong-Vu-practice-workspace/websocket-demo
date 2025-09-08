import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import Layout from './layout.tsx'
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import LoginPage from "./pages/client/auth/login.tsx";
import RegisterPage from "./pages/client/auth/register.tsx";
import {App, ConfigProvider} from "antd";
import {AppProvider} from "./components/context/app.context.tsx";
import enUS from 'antd/locale/en_US';

const router = createBrowserRouter(
    [
        {
            path: "/",
            element: <Layout/>,

        },
        {
            path: "/login",
            element: <LoginPage/>
        },
        {
            path: "/register",
            element: <RegisterPage/>
        }
    ]
)
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
