import {useEffect} from "react";
import {Outlet} from "react-router-dom";
import {Spin} from "antd";
import {useCurrentApp} from "./components/context/app.context.tsx";
import { fetchAccountAPI } from "./services/api.ts";
import AppHeader from "./components/layout/app.header.tsx";

function Layout() {
    const {setUser, setIsAuthenticated, isAppLoading, setIsAppLoading} = useCurrentApp();

    useEffect(() => {
        const fetchAccount = async () => {
            const res = await  fetchAccountAPI();
            if (res.data) {
                setUser(res.data.user);
                setIsAuthenticated(true);
            }
            setIsAppLoading(false);
        }
        fetchAccount();
    }, [])
    return (
        <>
            {
                !isAppLoading ?
                    <div>
                        <AppHeader />
                        <Outlet/>
                    </div>
                    :
                    <div>
                        <div style={{
                            position:"fixed",
                            top:"50%",
                            left:"50%",
                            transform: "translate(-50%,-50%)"

                        }}>
                            <Spin />
                        </div>
                    </div>
            }

        </>
    )
}

export default Layout
