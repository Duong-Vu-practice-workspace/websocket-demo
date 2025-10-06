import { useEffect, useState } from 'react';
import { Card, Input, Button, List, Typography, notification, Space } from 'antd';
import { sseService, type BackupData } from '../../../services/sse'; // Adjust path if needed
import { createBackupAPI } from "../../../services/api";

interface BackupItem {
    id: number;
    name: string;
    status: string;
}

const SseBackupPage = () => {
    const [name, setName] = useState<string>('');
    const [creating, setCreating] = useState<boolean>(false);
    const [backups, setBackups] = useState<BackupItem[]>([]);

    useEffect(() => {
        // Retrieve clientID from localStorage
        const clientID = localStorage.getItem('username') || 'defaultClientID'; // Fallback if not found
        const vitebackendUrl = import.meta.env.VITE_BACKEND_URL;
        const sseUrl = `${vitebackendUrl}/api/v1/backup/stream?clientID=${clientID}`;

        // Connect to SSE with the constructed URL
        sseService.connect(sseUrl);

        // Register listener for 'sse-event'
        const listenerId = sseService.onEvent((data: BackupData) => {
            console.log('SseBackupPage received SSE payload:', data);
            if (data == null) {
                console.warn('SseBackupPage: null payload received');
                return;
            }
            const incomingId: number | undefined = data.id;
            const incomingStatus: string | undefined = data.status;

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

        // Cleanup on unmount
        return () => {
            sseService.offEvent(listenerId);
            sseService.disconnect();
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
            if (id) {
                const newBackup: BackupItem = { id: Number(id), name: res.data.name.trim(), status: res.data.status };
                setBackups(prev => [newBackup, ...prev]);  // Add to the top of the list
                setName('');  // Clear input
                notification.info({ message: 'Backup started', description: `Backup id ${id}` });
            } else {
                throw new Error('Invalid response');
            }
        } catch (e) {
            notification.error({ message: 'Create backup failed' });
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
            <Card title="Create Backup (SSE)" style={{ marginBottom: 20 }}>
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

            <Card title="Backup History (SSE Updates)">
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

export default SseBackupPage;