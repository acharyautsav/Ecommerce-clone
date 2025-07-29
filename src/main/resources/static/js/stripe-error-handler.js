// Stripe Error Handler - Suppresses ad blocker related console errors
(function() {
    'use strict';
    
    // Store original console methods
    const originalConsoleError = console.error;
    const originalConsoleWarn = console.warn;
    
    // Suppress console errors from ad blockers and Stripe analytics
    console.error = function(...args) {
        const message = args.join(' ');
        if (message.includes('r.stripe.com') || 
            message.includes('ERR_BLOCKED_BY_CLIENT') ||
            message.includes('Cannot find module') ||
            message.includes('Failed to fetch') ||
            message.includes('net::ERR_BLOCKED_BY_CLIENT')) {
            // Suppress ad blocker related errors
            return;
        }
        originalConsoleError.apply(console, args);
    };
    
    // Suppress console warnings from Stripe
    console.warn = function(...args) {
        const message = args.join(' ');
        if (message.includes('payment method types are not activated') ||
            message.includes('link') ||
            message.includes('Stripe.js')) {
            // Suppress Stripe configuration warnings
            return;
        }
        originalConsoleWarn.apply(console, args);
    };
    
    // Handle unhandled promise rejections
    window.addEventListener('unhandledrejection', function(event) {
        const message = event.reason?.message || event.reason?.toString() || '';
        if (message.includes('r.stripe.com') || 
            message.includes('ERR_BLOCKED_BY_CLIENT') ||
            message.includes('Failed to fetch') ||
            message.includes('net::ERR_BLOCKED_BY_CLIENT')) {
            event.preventDefault();
            return false;
        }
    });
    
    // Handle general errors
    window.addEventListener('error', function(event) {
        const message = event.message || event.filename || '';
        if (message.includes('r.stripe.com') || 
            message.includes('ERR_BLOCKED_BY_CLIENT') ||
            message.includes('Failed to fetch')) {
            event.preventDefault();
            return false;
        }
    });
    
    console.log('Stripe error handler loaded - suppressing ad blocker related errors');
})();