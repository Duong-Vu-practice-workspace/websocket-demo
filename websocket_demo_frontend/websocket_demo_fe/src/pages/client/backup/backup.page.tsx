import { useEffect, useState } from 'react';
import { Card, Input, Button, Spin, List, Typography, notification } from 'antd';
import { addWSListener } from '../../../services/websocket';
import {createBackupAPI, fetchBackupByIdAPI} from "../../../services/api.ts";



const BackupPage = () => {
    const [name, setName] = useState<string>('');
    const [creating, setCreating] = useState<boolean>(false);
    const [backupId, setBackupId] = useState<string | null>(null);
    const [status, setStatus] = useState<string | null>(null);
    const [history, setHistory] = useState<string[]>([]);

    useEffect(() => {
        const unsub = addWSListener(async (payload: any) => {
            console.log('BackupPage received WS payload:', payload);  // Add this for debugging
            let incomingId: string | undefined;
            let incomingStatus: string | undefined;
            if (payload == null) {
                console.warn('BackupPage: null payload received');
                return;
            }
            if (typeof payload === 'string' || typeof payload === 'number') {
                incomingId = String(payload);
                console.log('BackupPage: treating payload as plain id:', incomingId);
            } else if (payload.id) {
                incomingId = String(payload.id);
                incomingStatus = payload.status;
                console.log('BackupPage: parsed JSON payload - id:', incomingId, 'status:', incomingStatus);
            } else {
                console.warn('BackupPage: unknown payload format:', payload);
                return;
            }

            if (!incomingId) return;

            if (backupId && incomingId === backupId) {
                console.log('BackupPage: matching backupId, updating status');
                try {
                    const res = await fetchBackupByIdAPI(incomingId);
                    if (res && res.data) {
                        const b = res.data as any;
                        setStatus(b.status ?? incomingStatus ?? 'UNKNOWN');
                    } else if (incomingStatus) {
                        setStatus(incomingStatus);
                    }
                    setCreating(false);  // Stop spinner
                    notification.success({
                        message: `Backup ${incomingId} updated`,
                        description: `Status: ${incomingStatus ?? 'updated'}`,
                    });
                    setHistory(prev => [`updated ${incomingId}: ${incomingStatus ?? 'updated'}`, ...prev]);
                } catch (err) {
                    console.warn('BackupPage: Failed to fetch backup:', err);
                    setCreating(false);  // Stop spinner on error
                }
            } else {
                console.log('BackupPage: incomingId does not match backupId:', incomingId, 'vs', backupId);
            }
        });

        return () => {
            unsub();
        };
    }, [backupId]);

    const handleCreate = async () => {
        if (!name) {
            notification.warning({ message: 'Please provide a name' });
            return;
        }
        setCreating(true);
        try {
            const res = await createBackupAPI(name);
            // controller returns saved id as plain string in body
            const id = res && (res.data ?? res) ? (res.data ?? res) : null;
            const idStr = String(id);
            setBackupId(idStr);
            setStatus('CREATING');
            setHistory(prev => [`created ${idStr}`, ...prev]);
            notification.info({ message: 'Backup started', description: `Backup id ${idStr}` });
            // keep spinner until websocket event arrives
        } catch (e) {
            setCreating(false);
            notification.error({ message: 'Create backup failed' });
        }
    };

    return (
        <div style={{ padding: 20 }}>
            <Card title="Create Backup" style={{ marginBottom: 20 }}>
                <Input
                    placeholder="Backup name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    style={{ marginBottom: 12 }}
                />
                <Button type="primary" onClick={handleCreate} loading={creating}>
                    Create
                </Button>
                {creating && (
                    <div style={{ marginTop: 12 }}>
                        <Spin /> Creating...
                    </div>
                )}
            </Card>

            <Card title="Backup Status" style={{ marginBottom: 20 }}>
                <p>Backup Id: {backupId ?? '-'}</p>
                <p>Status: {status ?? '-'}</p>
            </Card>

            <Card title="Events / History">
                <List
                    dataSource={history}
                    renderItem={(item, idx) => (
                        <List.Item key={idx}>
                            <Typography.Text code>{item}</Typography.Text>
                        </List.Item>
                    )}
                    locale={{ emptyText: 'No events yet' }}
                />
            </Card>
        </div>
    );
};

export default BackupPage;