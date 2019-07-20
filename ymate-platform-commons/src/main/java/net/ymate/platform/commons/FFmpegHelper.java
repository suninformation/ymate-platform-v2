/*
 * Copyright 2007-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.platform.commons;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 刘镇 (suninformation@163.com) on 16/7/22 下午7:02
 */
public class FFmpegHelper {

    private static final Log LOG = LogFactory.getLog(FFmpegHelper.class);

    private static final String ZERO_STR = "0";

    private String ffmpegPath;

    private String mediaFile;

    private boolean writeLog = true;

    public static FFmpegHelper create() {
        return new FFmpegHelper("ffmpeg");
    }

    public static FFmpegHelper create(String ffmpegPath) {
        return new FFmpegHelper(ffmpegPath);
    }

    public static int reduceTimeLen(String timeLen) {
        int min = 0;
        String[] strArr = timeLen.split(":");
        if (strArr[0].compareTo(ZERO_STR) > 0) {
            min += Integer.valueOf(strArr[0]) * 60 * 60;
        }
        if (strArr[1].compareTo(ZERO_STR) > 0) {
            min += Integer.valueOf(strArr[1]) * 60;
        }
        if (strArr[2].compareTo(ZERO_STR) > 0) {
            min += Math.round(Float.valueOf(strArr[2]));
        }
        return min;
    }

    public static String buildTimeStr(int time) {
        int h = time / (60 * 60);
        time = time - h * 60 * 60;
        int m = time / 60;
        time = time - m * 60;
        int s = time;
        return String.format("%d:%d:%d", h, m, s);
    }

    public static String buildResolutionStr(int imageWidth, int imageHeight) {
        if (imageWidth > 0 && imageHeight > 0) {
            return String.format("%dx%d", imageWidth, imageHeight);
        }
        return null;
    }

    private FFmpegHelper(String ffmpegPath) {
        if (StringUtils.isBlank(ffmpegPath) && doCheckFile(ffmpegPath)) {
            throw new IllegalArgumentException("Argument ffmpegPath illegal.");
        }
        this.ffmpegPath = ffmpegPath;
    }

    public FFmpegHelper bind(String mediaFile) {
        if (!doCheckFile(mediaFile)) {
            throw new IllegalArgumentException("Argument mediaFile illegal.");
        }
        this.mediaFile = mediaFile;
        return this;
    }

    public FFmpegHelper bind(File mediaFile) {
        return bind(mediaFile == null ? null : mediaFile.getPath());
    }

    public FFmpegHelper writeLog(boolean writeLog) {
        this.writeLog = writeLog;
        return this;
    }

    private boolean doCheckFile(String file) {
        if (StringUtils.isNotBlank(file)) {
            File fileObj = new File(file);
            return fileObj.exists() && fileObj.isFile();
        }
        return false;
    }

    public MediaInfo getMediaInfo() {
        try {
            String outputStr = ConsoleCmdExecutor.exec(ffmpegPath, "-i", mediaFile);
            if (writeLog && LOG.isInfoEnabled()) {
                LOG.info(outputStr);
            }
            //从视频信息中解析时长
            String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb/s";
            String regexVideo = "Video: (.*?), (.*?), (.*?)[,\\s]";
            String regexAudio = "Audio: (\\w*), (\\d*) Hz";
            //
            MediaInfo mediaInfo = new MediaInfo();
            //
            Pattern pattern = Pattern.compile(regexDuration);
            Matcher m = pattern.matcher(outputStr);
            if (m.find()) {
                mediaInfo.setStart(BlurObject.bind(m.group(2)).toIntValue());
                mediaInfo.setBitrates(BlurObject.bind(m.group(3)).toIntValue());
                mediaInfo.setTime(reduceTimeLen(m.group(1)));
            }
            pattern = Pattern.compile(regexVideo);
            m = pattern.matcher(outputStr);
            if (m.find()) {
                mediaInfo.setVideoEncodingFormat(m.group(1));
                mediaInfo.setVideoFormat(m.group(2));
                mediaInfo.setResolution(m.group(3));
            }
            pattern = Pattern.compile(regexAudio);
            m = pattern.matcher(outputStr);
            if (m.find()) {
                mediaInfo.setAudioEncodingFormat(m.group(1));
                mediaInfo.setAudioSamplingRate(m.group(2));
            }
            return mediaInfo;
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    public File convertAudio(String aCodec, File outputFile) {
        try {
            ConsoleCmdExecutor.exec(new String[]{ffmpegPath, "-y", "-i", mediaFile, "-acodec", StringUtils.defaultIfBlank(aCodec, "copy"), "-vn", outputFile.getPath()}, new ICmdOutputHandler.WriteConsoleLog(writeLog));
            return (outputFile.exists() && outputFile.length() > 0) ? outputFile : null;
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    public File screenshotVideo(int startSecond, int imageWidth, int imageHeight, int time, File outputJpeg) {
        // ffmpeg -i input.mp4 -r 1 -q:v 2 -f image2 pic-%03d.jpeg
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(mediaFile);
        if (startSecond > 0) {
            // 设置截取视频画面时间
            cmd.add("-ss");
            cmd.add(buildTimeStr(startSecond));
        }
        cmd.add("-t");
        if (time <= 0) {
            cmd.add("0.001");
        } else {
            cmd.add(String.valueOf(time));
        }
        cmd.add("-r");
        cmd.add("1");
        cmd.add("-q:v");
        cmd.add("2");
        cmd.add("-f");
        cmd.add("image2");
        //
        return execCmd(cmd, buildResolutionStr(imageWidth, imageHeight), outputJpeg);
    }

    public File videoScale(int imageWidth, int imageHeight, File output) {
        // ffmpeg -i input.mp4 -vf scale=960:540 output.mp4
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(mediaFile);
        cmd.add("-vf");
        cmd.add("scale=" + imageWidth + ":" + imageHeight);
        //
        return execCmd(cmd, buildResolutionStr(imageWidth, imageHeight), output);
    }

    public File videoToFlv(int imageWidth, int imageHeight, File outputFlv) {
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(mediaFile);
        cmd.add("-ab");
        cmd.add("128");
        cmd.add("-ar");
        cmd.add("22050");
        cmd.add("-b");
        cmd.add("800");
        //
        return execCmd(cmd, buildResolutionStr(imageWidth, imageHeight), outputFlv);
    }

    public File videoOverlayLogo(File imageFile, boolean topLeft, File output) {
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(mediaFile);
        cmd.add("-i");
        cmd.add(imageFile.getPath());
        cmd.add("-filter_complex");
        if (topLeft) {
            cmd.add("overlay=main_w-overlay_w-10:main_h-overlay_h-10");
        } else {
            cmd.add("overlay");
        }
        //
        return execCmd(cmd, null, output);
    }

    public File videoCut(int startSecond, int endSecond, String vCodec, String aCodec, File outputFile) {
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(mediaFile);
        cmd.add("-ss");
        cmd.add(buildTimeStr(startSecond));
        cmd.add("-t");
        cmd.add(buildTimeStr(endSecond));
        cmd.add("-vcodec");
        cmd.add(StringUtils.defaultIfBlank(vCodec, "copy"));
        cmd.add("-acodec");
        cmd.add(StringUtils.defaultIfBlank(aCodec, "copy"));
        //
        return execCmd(cmd, null, outputFile);
    }

    private File execCmd(List<String> cmd, String imageSize, File outputFile) {
        if (StringUtils.isNotBlank(imageSize)) {
            // 设置截图大小
            cmd.add("-s");
            cmd.add(imageSize);
        }
        if (outputFile != null) {
            cmd.add(outputFile.getPath());
        }
        //
        try {
            ConsoleCmdExecutor.exec(cmd, new ICmdOutputHandler.WriteConsoleLog(writeLog));
            return (outputFile != null && outputFile.exists() && outputFile.length() > 0) ? outputFile : null;
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    public static class MediaInfo implements Serializable {

        private int start;

        private int bitrates;

        private int time;

        private String videoEncodingFormat;

        private String videoFormat;

        private String resolution;

        private int imageWidth;

        private int imageHeight;

        private String audioEncodingFormat;

        private String audioSamplingRate;

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getBitrates() {
            return bitrates;
        }

        public void setBitrates(int bitrates) {
            this.bitrates = bitrates;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public String getVideoEncodingFormat() {
            return videoEncodingFormat;
        }

        public void setVideoEncodingFormat(String videoEncodingFormat) {
            this.videoEncodingFormat = videoEncodingFormat;
        }

        public String getVideoFormat() {
            return videoFormat;
        }

        public void setVideoFormat(String videoFormat) {
            this.videoFormat = videoFormat;
        }

        public String getResolution() {
            return resolution;
        }

        public void setResolution(String resolution) {
            this.resolution = resolution;
            if (StringUtils.isNotBlank(resolution)) {
                String[] arr = StringUtils.split(resolution, "x");
                if (arr != null && arr.length == 2) {
                    this.imageWidth = BlurObject.bind(arr[0]).toIntValue();
                    this.imageHeight = BlurObject.bind(arr[1]).toIntValue();
                }
            }
        }

        public int getImageWidth() {
            return imageWidth;
        }

        public int getImageHeight() {
            return imageHeight;
        }

        public String getAudioEncodingFormat() {
            return audioEncodingFormat;
        }

        public void setAudioEncodingFormat(String audioEncodingFormat) {
            this.audioEncodingFormat = audioEncodingFormat;
        }

        public String getAudioSamplingRate() {
            return audioSamplingRate;
        }

        public void setAudioSamplingRate(String audioSamplingRate) {
            this.audioSamplingRate = audioSamplingRate;
        }

        @Override
        public String toString() {
            return String.format("MediaInfo {start=%d, bitrates=%d, time=%d, videoEncodingFormat='%s', videoFormat='%s', resolution='%s', audioEncodingFormat='%s', audioSamplingRate='%s'}", start, bitrates, time, videoEncodingFormat, videoFormat, resolution, audioEncodingFormat, audioSamplingRate);
        }
    }
}
