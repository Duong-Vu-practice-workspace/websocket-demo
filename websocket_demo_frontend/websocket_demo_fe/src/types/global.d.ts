export { };
declare global {
    interface IModelPaginate<T> {
        meta: {
            current: number;
            pageSize: number;
            pages: number;
            total: number;
        },
        result: T[]
    }
    interface IBackendRes<T> {
        error?: string | string[];
        message: string;
        status: string;
        statusCode: number;
        data?: T;
        timestamp: string
    }
    interface ILogin {
        access_token: string;
        user: {
            email: string;
            username: string;
            fullName: string;
            role: string;
            id: string;
        }
    }
    interface IRegister {
        id: string;
        email: string;
        fullName: string;
    }

    interface IUser {
        email: string;
        username: string;
        fullName: string;
        role: string;
        id: string;
    }

    interface IFetchAccount {
        user: IUser
    }
    interface IMeta {
        page: number|null,
        pageSize: number,
        pages: number,
        total: number
    }
}