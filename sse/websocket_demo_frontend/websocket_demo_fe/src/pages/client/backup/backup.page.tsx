import { useEffect, useState } from 'react';
import { Card, Input, Button, List, Typography, notification, Space } from 'antd';
import { addWSListener } from '../../../services/websocket';
import { createBackupAPI } from "../../../services/api.ts";


interface PayLoadBackup {
    id: string;
    status: string;
}
const BackupPage = () => {
    const [name, setName] = useState<string>('');
    const [creating, setCreating] = useState<boolean>(false);
    const [backups, setBackups] = useState<BackupItem[]>([]);

    useEffect(() => {
        const unsub = addWSListener(async (payload: PayLoadBackup) => {
            console.log('BackupPage received WS payload:', payload);
            if (payload == null) {
                console.warn('BackupPage: null payload received');
                return;
            }
            const incomingId: string | undefined = payload.id;
            const incomingStatus: string | undefined = payload.status;

            if (!incomingId) return;

            // Find and update the matching backup in the list
            setBackups(prevBackups =>
                prevBackups.map(backup =>
                    backup.id === incomingId
                        ? { ...backup, status: incomingStatus ?? backup.status }
                        : backup
                )
            );

            notification.success({
                message: `Backup ${incomingId} updated`,
                description: `Status: ${incomingStatus ?? 'updated'}`,
            });
        });

        return () => {
            unsub();
        };
    }, []);

    const handleCreate = async () => {
        if (!name.trim()) {
            notification.warning({ message: 'Please provide a backup name' });
            return;
        }
        setCreating(true);
        try {
            const res = await createBackupAPI(name.trim());
            const id = res.data.id;
            // const id = res && (res.data ?? res) ? String(res.data ?? res) : null;
            if (id) {
                const newBackup: BackupItem = { id, name: res.data.name.trim(), status: res.data.status };
                setBackups(prev => [newBackup, ...prev]);  // Add to the top of the list
                setName('');  // Clear input
                notification.info({ message: 'Backup started', description: `Backup id ${id}` });
            } else {
                throw new Error('Invalid response');
            }
        } catch (e) {
            notification.error({ message: 'Create backup failed'});
        } finally {
            setCreating(false);
        }
    };

    const handleClearHistory = () => {
        setBackups([]);
        notification.info({ message: 'Backup history cleared' });
    };

    return (
        <div style={{ padding: 20 }}>
            <Card title="Create Backup" style={{ marginBottom: 20 }}>
                <Space direction="vertical" style={{ width: '100%' }}>
                    <Input
                        placeholder="Backup name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        onPressEnter={handleCreate}
                        disabled={creating}
                    />
                    <Space>
                        <Button type="primary" onClick={handleCreate} loading={creating}>
                            Create Backup
                        </Button>
                        <Button onClick={handleClearHistory} disabled={backups.length === 0}>
                            Clear History
                        </Button>
                    </Space>
                </Space>
            </Card>

            <Card title="Backup History">
                <List
                    dataSource={backups}
                    renderItem={(backup, idx) => (
                        <List.Item key={idx}>
                            <Space direction="vertical" style={{ width: '100%' }}>
                                <Typography.Text strong>{backup.name} (ID: {backup.id})</Typography.Text>
                                <Typography.Text type={backup.status === 'COMPLETED' ? 'success' : backup.status === 'CREATING' ? 'warning' : 'danger'}>
                                    Status: {backup.status}
                                </Typography.Text>
                            </Space>
                        </List.Item>
                    )}
                    locale={{ emptyText: 'No backups created yet' }}
                    style={{ maxHeight: '400px', overflowY: 'auto' }}
                />
            </Card>
        </div>
    );
};

export default BackupPage;