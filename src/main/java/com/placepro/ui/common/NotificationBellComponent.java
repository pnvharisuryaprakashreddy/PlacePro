package com.placepro.ui.common;

import com.placepro.service.notification.NotificationService;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;

/**
 * Reusable notification bell for dashboard top bars. Shows the current user's
 * unread count as a badge and opens the notification inbox when clicked.
 *
 * Polls every {@link #POLL_INTERVAL_MS} using a javax.swing.Timer; the timer
 * fires on the EDT and the actual database call runs on a background worker.
 * Polling starts when the component is added to a visible hierarchy and stops
 * when it is removed (e.g. on logout), so no stale polling survives the screen.
 */
public class NotificationBellComponent extends JButton {

    private static final int POLL_INTERVAL_MS = 20_000;
    private static final String BELL = "\uD83D\uDD14";

    private final NotificationService notificationService;
    private final Timer pollTimer;

    public NotificationBellComponent(NotificationService notificationService) {
        super(BELL);
        this.notificationService = notificationService;
        setToolTipText("Notifications");
        setFocusPainted(false);
        setMargin(new java.awt.Insets(2, 8, 2, 8));
        setPreferredSize(new Dimension(72, 28));
        addActionListener(event -> openInbox());

        pollTimer = new Timer(POLL_INTERVAL_MS, event -> refreshUnreadCount());
        pollTimer.setInitialDelay(0);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        pollTimer.start();
    }

    @Override
    public void removeNotify() {
        pollTimer.stop();
        super.removeNotify();
    }

    public void refreshUnreadCount() {
        UiTasks.run(
                notificationService::getUnreadCountForCurrentUser,
                this::showUnreadCount,
                exception -> {
                    // Session may have ended or the DB may be briefly unreachable;
                    // fail silently rather than interrupting the user every poll.
                    setText(BELL);
                    setForeground(null);
                });
    }

    private void showUnreadCount(int unreadCount) {
        if (unreadCount > 0) {
            setText(BELL + " " + (unreadCount > 99 ? "99+" : unreadCount));
            setForeground(new Color(198, 40, 40));
            setToolTipText(unreadCount + " unread notification(s)");
        } else {
            setText(BELL);
            setForeground(null);
            setToolTipText("No unread notifications");
        }
    }

    private void openInbox() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Notifications", JDialog.ModalityType.APPLICATION_MODAL);
        NotificationInboxPanel inboxPanel = new NotificationInboxPanel(
                notificationService,
                this::refreshUnreadCount);
        dialog.setContentPane(inboxPanel);
        dialog.setSize(560, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        refreshUnreadCount();
    }
}
