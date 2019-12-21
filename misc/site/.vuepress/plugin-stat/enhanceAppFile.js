export default ({router}) => {
    if (process.env.NODE_ENV === 'production' && typeof window !== 'undefined') {
        const script = document.createElement('script');
        script.src = 'https://s4.cnzz.com/z_stat.php?id=1254908110&web_id=1254908110';
        script.language = 'JavaScript';
        document.body.appendChild(script);

        router.afterEach(function (to) {
            if (to.path) {
                if (window._czc) {
                    window._czc.push(['_trackPageview', to.fullPath, '/'])
                }
                //
                const _hmt = _hmt || [];
                window._hmt = _hmt;
                (function () {
                    document.getElementById('baidu_stat') && document.getElementById('baidu_stat').remove();
                    //
                    const hm = document.createElement("script");
                    hm.id = 'baidu_stat';
                    hm.src = 'https://hm.baidu.com/hm.js?d732d09a2ccea77b26ad0581cd9bd91c';
                    const s = document.getElementsByTagName("script")[0];
                    s.parentNode.insertBefore(hm, s);
                })();
            }
        })
    }
}

