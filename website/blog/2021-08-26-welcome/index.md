---
slug: welcome
title: Welcome
authors: suninformation
tags: [java, webmvc, plugin, serv, cache, orm, mongodb, jdbc, redis]
keywords: [java, webmvc, plugin, serv, cache, orm, mongodb, jdbc, redis]
description: description
image: /img/logo.png
draft: true
hide_table_of_contents: false
---

<head>
  <html className="some-extra-html-class" />
  <body className="other-extra-body-class" />
  <title>Head Metadatas customized title!</title>
  <meta charSet="utf-8" />
  <meta name="twitter:card" content="summary" />
  <link rel="canonical" href="https://docusaurus.io/docs/markdown-features/head-metadatas" />
</head>

YMP 是一个非常简单、易用的一套轻量级 Java 应用开发框架，设计原则主要侧重于简化工作任务、规范开发流程、提高开发效率，让开发工作像搭积木一样轻松是我们一直不懈努力的目标！

<!--truncate-->

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

## 选项卡示例

<Tabs>
  <TabItem value="apple" label="Apple" default>
    This is an apple 🍎
  </TabItem>
  <TabItem value="orange" label="Orange">
    This is an orange 🍊
  </TabItem>
  <TabItem value="banana" label="Banana">
    This is a banana 🍌
  </TabItem>
</Tabs>

<Tabs
defaultValue="apple"
values={[
{label: 'Apple', value: 'apple'},
{label: 'Orange', value: 'orange'},
{label: 'Banana', value: 'banana'},
]}>
<TabItem value="apple">This is an apple 🍎</TabItem>
<TabItem value="orange">This is an orange 🍊</TabItem>
<TabItem value="banana">This is a banana 🍌</TabItem>
</Tabs>

<Tabs groupId="operating-systems">
  <TabItem value="win" label="Windows" default>Use Ctrl + C to copy.</TabItem>
  <TabItem value="mac" label="MacOS">Use Command + C to copy.</TabItem>
</Tabs>

<Tabs groupId="operating-systems">
  <TabItem value="win" label="Windows" default>Use Ctrl + V to paste.</TabItem>
  <TabItem value="mac" label="MacOS">Use Command + V to paste.</TabItem>
</Tabs>


<Tabs groupId="operating-systems">
  <TabItem value="win" label="Windows" default>
    I am Windows.
  </TabItem>
  <TabItem value="mac" label="MacOS">
    I am macOS.
  </TabItem>
  <TabItem value="linux" label="Linux">
    I am Linux.
  </TabItem>
</Tabs>

## 代码块示例

```jsx title="/src/components/HelloCodeTitle.js"
function HelloCodeTitle(props) {
  return <h1>你好，{props.name}</h1>;
}
```

```jsx {3}
function HighlightSomeText(highlight) {
  if (highlight) {
    return '这句被高亮了！';
  }

  return '这句没有';
}
```

```jsx {1,4-6,11}
import React from 'react';

function MyComponent(props) {
  if (props.isBar) {
    return <div>Bar</div>;
  }

  return <div>Foo</div>;
}

export default MyComponent;
```

```jsx
function HighlightSomeText(highlight) {
  if (highlight) {
    // highlight-next-line
    return '这句被高亮了！';
  }

  return '这里不会';
}

function HighlightMoreText(highlight) {
  // highlight-start
  if (highlight) {
    return '这片区域被高亮了！';
  }
  // highlight-end

  return '这里不会';
}
```

<Tabs
defaultValue="js"
values={[
{ label: 'JavaScript', value: 'js', },
{ label: 'Python', value: 'py', },
{ label: 'Java', value: 'java', },
]
}>
<TabItem value="js">

```js
function helloWorld() {
  console.log('Hello, world!');
}
```

</TabItem>
<TabItem value="py">

```py
def hello_world():
  print 'Hello, world!'
```

</TabItem>
<TabItem value="java">

```java
class HelloWorld {
  public static void main(String args[]) {
    System.out.println("Hello, World");
  }
}
```

</TabItem>
</Tabs>

## 告示示例

:::note

Some **content** with _markdown_ `syntax`. Check [this `api`](#).

:::

:::tip

Some **content** with _markdown_ `syntax`. Check [this `api`](#).

:::

:::info

Some **content** with _markdown_ `syntax`. Check [this `api`](#).

:::

:::caution

Some **content** with _markdown_ `syntax`. Check [this `api`](#).

:::

:::danger

Some **content** with _markdown_ `syntax`. Check [this `api`](#).

:::

:::tip Use tabs in admonitions

<Tabs
defaultValue="apple"
values={[
{label: 'Apple', value: 'apple'},
{label: 'Orange', value: 'orange'},
{label: 'Banana', value: 'banana'},
]}>
<TabItem value="apple">This is an apple 🍎</TabItem>
<TabItem value="orange">This is an orange 🍊</TabItem>
<TabItem value="banana">This is a banana 🍌</TabItem>
</Tabs>

:::


## 内联目录

import TOCInline from '@theme/TOCInline';

<TOCInline toc={toc} />
