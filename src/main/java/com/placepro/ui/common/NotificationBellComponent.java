package com.placepro.ui.common;

import com.placepro.service.notification.NotificationService;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Enterprise Notification Bell with anti-aliased vector badge.
 */
public class NotificationBellComponent extends JButton {

    private static final int POLL_INTERVAL_MS = 20_000;
    private final NotificationService notificationService;
    private final Timer pollTimer;
    private int unreadCount = 0;

    public NotificationBellComponent(NotificationService notificationService) {
        super();
        this.notificationService = notificationService;
        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Notifications");
        setPreferredSize(new Dimension(84, 34));
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
                count -> {
                    this.unreadCount = count;
                    repaint();
                },
                exception -> {
                    this.unreadCount = 0;
                    repaint();
                });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Button rounded pill background
        boolean isHover = getModel().isRollover();
        g2.setColor(isHover ? UiStyles.SURFACE_ALT_COLOR : UiStyles.SURFACE_COLOR);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
        g2.setColor(UiStyles.BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 12, 12));

        // Draw bell symbol / label text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(UiStyles.TEXT_COLOR);
        g2.drawString("🔔 Alert", 12, h / 2 + 4);

        // Draw unread badge bubble if unreadCount > 0
        if (unreadCount > 0) {
            String badgeText = unreadCount > 99 ? "99+" : String.valueOf(unreadCount);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int bw = Math.max(18, fm.stringWidth(badgeText) + 8);
            int bh = 18;
            int bx = w - bw - 4;
            int by = (h - bh) / 2;

            g2.setColor(UiStyles.ERROR_COLOR);
            g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 18, 18));

            g2.setColor(Color.WHITE);
            g2.drawString(badgeText, bx + (bw - fm.stringWidth(badgeText)) / 2, by + 13);
        }

        g2.dispose();
    }

    private void openInbox() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Notification Center", JDialog.ModalityType.APPLICATION_MODAL);
        NotificationInboxPanel inboxPanel = new NotificationInboxPanel(
                notificationService,
                this::refreshUnreadCount);
        dialog.setContentPane(inboxPanel);
        dialog.setSize(600, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        refreshUnreadCount();
    }
}
