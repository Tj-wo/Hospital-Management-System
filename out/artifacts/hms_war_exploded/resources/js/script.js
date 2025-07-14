
document.addEventListener('DOMContentLoaded', function() {
    const appLayout = document.querySelector('.app-layout');
    const sidebar = document.querySelector('.sidebar');

    if (appLayout && sidebar) {
        // Create mobile toggle functionality using CSS pseudo-elements
        appLayout.addEventListener('click', function(e) {
            const rect = appLayout.getBoundingClientRect();
            const clickX = e.clientX - rect.left;
            const clickY = e.clientY - rect.top;


            if (clickX <= 60 && clickY <= 60 && window.innerWidth <= 768) {
                sidebar.classList.toggle('mobile-open');
            }
        });

        sidebar.addEventListener('click', function(e) {
            if (e.target === sidebar && sidebar.classList.contains('mobile-open')) {
                sidebar.classList.remove('mobile-open');
            }
        });
    }
});