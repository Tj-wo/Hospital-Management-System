document.addEventListener('DOMContentLoaded', function() {
    const mobileToggle = document.querySelector('.mobile-nav-toggle');
    const sidebar = document.querySelector('.sidebar');
    const overlay = document.querySelector('.mobile-overlay');

    // Function to toggle all navigation elements
    const toggleNav = () => {
        if (mobileToggle && sidebar && overlay) {
            mobileToggle.classList.toggle('active');
            sidebar.classList.toggle('mobile-open');
            overlay.classList.toggle('active');
        }
    };

    // Add click listeners if the elements exist
    if (mobileToggle && sidebar && overlay) {
        mobileToggle.addEventListener('click', toggleNav);
        overlay.addEventListener('click', toggleNav);
    }
});