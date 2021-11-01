import React from 'react';
import clsx from 'clsx';
import Translate, {translate} from '@docusaurus/Translate';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
    {
        title: translate({id: "home.feature.lightweight.title", message: "轻量级"}),
        Svg: require('../../static/img/undraw_docusaurus_mountain.svg').default,
        description: (
            <>
                <Translate id="home.feature.lightweight.description">采用微内核实现 AutoScan、AOP、IoC、Events 等特性，涵盖 SSH 和 SSM 框架中绝大部分核心功能。</Translate>
            </>
        ),
    },
    {
        title: translate({id: "home.feature.modularization.title", message: "组件化"}),
        Svg: require('../../static/img/undraw_docusaurus_tree.svg').default,
        description: (
            <>
                <Translate id="home.feature.modularization.description">采用模块方式打包，按需装配，灵活扩展，独特的服务开发体验，完善的插件机制，助力于更细颗粒度的业务拆分。</Translate>
            </>
        ),
    },
    {
        title: translate({id: "home.feature.simple_efficient.title", message: "简单、高效"}),
        Svg: require('../../static/img/undraw_docusaurus_react.svg').default,
        description: (
            <>
                <Translate id="home.feature.simple_efficient.description">统一日志系统和配置体系结构，轻封装持久化层，灵活的缓存服务，配置简单的 MVC 和参数验证，让您更专注于业务。</Translate>
            </>
        ),
    },
];

function Feature({Svg, title, description}) {
    return (
        <div className={clsx('col col--4')}>
            <div className="text--center">
                <Svg className={styles.featureSvg} alt={title}/>
            </div>
            <div className="text--center padding-horiz--md">
                <h3>{title}</h3>
                <p>{description}</p>
            </div>
        </div>
    );
}

export default function HomepageFeatures() {
    return (
        <section className={styles.features}>
            <div className="container">
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
