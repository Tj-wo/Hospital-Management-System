document.addEventListener('DOMContentLoaded', function () {
    const mobileToggle = document.querySelector('.mobile-nav-toggle');
    const sidebar = document.querySelector('.sidebar');
    const overlay = document.querySelector('.mobile-overlay');

    // Function to toggle navigation
    const toggleNav = () => {
        if (mobileToggle && sidebar && overlay) {
            mobileToggle.classList.toggle('active');
            sidebar.classList.toggle('mobile-open');
            overlay.classList.toggle('active');

            // Prevent scrolling on body when sidebar is open
            document.body.style.overflow = sidebar.classList.contains('mobile-open') ? 'hidden' : '';
        }
    };

    // Add click listeners if elements exist
    if (mobileToggle && sidebar && overlay) {
        mobileToggle.addEventListener('click', toggleNav);
        overlay.addEventListener('click', toggleNav);
    }
});