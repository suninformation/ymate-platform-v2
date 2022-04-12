import React from 'react';
import clsx from 'clsx';
import Translate from '@docusaurus/Translate';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import HomepageFeatures from '../components/HomepageFeatures';

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <img src={siteConfig.customFields.logoBigUrl} alt="YMP Big Logo"/>
                <p className={clsx('hero__subtitle', styles.heroTagline)}><Translate id="home.hero.tagline">轻量级、组件化、简单、高效的 Java 应用开发框架</Translate></p>
                <p className={styles.heroTagline}><Translate id="home.hero.newVersion">最新版本：</Translate><b>2.1.1</b></p>
                <p className={styles.heroTagline}>
                    <a href="https://search.maven.org/#search%7Cga%7C1%7Cnet.ymate.platform" target="_blank"><img src="https://img.shields.io/maven-central/v/net.ymate.platform/ymate-platform-core.svg" alt="Maven Central Version"/></a>
                    &nbsp;
                    <a href="https://gitee.com/suninformation/ymate-platform-v2/blob/master/LICENSE.txt" target="_blank"><img src="https://img.shields.io/github/license/suninformation/ymate-platform-v2.svg" alt="License Info"/></a>
                </p>
                <div className={styles.buttons}>
                    <Link className="button button--primary button--lg" to="/quickstart"><Translate id="home.hero.quickstart">快速上手️ - 5分钟️</Translate> ⏱</Link>
                </div>
            </div>
        </header>
    );
}

export default function Home() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout
            title="Home"
            description="A lightweight modular simple and powerful Java application development framework.">
            <HomepageHeader/>
            <main>
                <HomepageFeatures/>
            </main>
        </Layout>
    );
}
