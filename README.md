# 🎬 VideoIntelligence

> 基于大模型的视频智能分析平台  
> 对视频进行自动转码、语音识别、内容理解，并利用大模型生成知识点大纲、详细知识点、思维导图、架构图等多维度分析结果。

![Java](https://img.shields.io/badge/Java-21+-orange.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-202x-green.svg)
![Vue](https://img.shields.io/badge/Vue-3.x-42b883.svg)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)
![FFmpeg](https://img.shields.io/badge/FFmpeg-Video%20Processing-black.svg)
![MinIO](https://img.shields.io/badge/MinIO-Object%20Storage-red.svg)
![AI](https://img.shields.io/badge/AI-LLM-purple.svg)
![License](https://img.shields.io/badge/License-Apache2.0-yellow.svg)
![License](https://img.shields.io/badge/License-python-blue.svg)

---

## 📖 项目简介

**VideoIntelligence** 是一个面向视频内容理解与知识提取的 AI 视频分析平台。

传统的视频观看方式需要用户完整观看视频后，再自行整理知识点、总结内容和构建知识体系，效率较低。

本项目希望通过 **AI + 视频处理 + 大语言模型 + 知识可视化**，将原始视频自动转换为结构化的知识内容。

用户只需要上传一个视频，平台即可自动完成：

```text
视频上传
   ↓
视频预处理
   ↓
音视频解析
   ↓
语音识别 ASR
   ↓
字幕/文本生成
   ↓
文本切分与上下文构建
   ↓
大模型分析
   ↓
知识提取
   ↓
多维度内容生成
   ↓
结果可视化