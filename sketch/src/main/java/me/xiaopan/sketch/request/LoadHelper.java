/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.sketch.request;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView.ScaleType;

import me.xiaopan.sketch.Configuration;
import me.xiaopan.sketch.Sketch;
import me.xiaopan.sketch.feture.RequestFactory;
import me.xiaopan.sketch.process.ImageProcessor;
import me.xiaopan.sketch.util.SketchUtils;

public class LoadHelper {
    protected static final String NAME = "LoadHelper";

    protected Sketch sketch;

    protected RequestAttrs attrs = new RequestAttrs();
    protected LoadOptions options = new LoadOptions();
    protected LoadListener listener;
    protected DownloadProgressListener progressListener;

    /**
     * 支持以下几种图片Uri
     * <blockQuote>"http://site.com/image.png"; // from Web
     * <br>"https://site.com/image.png"; // from Web
     * <br>"file:///mnt/sdcard/image.png"; // from SD card
     * <br>"/mnt/sdcard/image.png"; // from SD card
     * <br>"/mnt/sdcard/app.apk"; // from SD card apk file
     * <br>"content://media/external/audio/albumart/13"; // from content provider
     * <br>"asset://image.png"; // from assets
     * <br>"drawable://" + R.drawable.image; // from drawables (only images, non-9patch)
     * </blockQuote>
     */
    public LoadHelper(Sketch sketch, String uri) {
        this.sketch = sketch;
        this.attrs.reset(uri);
    }

    /**
     * 设置名称，用于在log总区分请求
     */
    public LoadHelper name(String name) {
        this.attrs.setName(name);
        return this;
    }

    /**
     * 关闭硬盘缓存
     */
    @SuppressWarnings("unused")
    public LoadHelper disableDiskCache() {
        options.setCacheInDisk(false);
        return this;
    }

    /**
     * 设置请求Level
     */
    @SuppressWarnings("unused")
    public LoadHelper requestLevel(RequestLevel requestLevel) {
        if (requestLevel != null) {
            options.setRequestLevel(requestLevel);
            options.setRequestLevelFrom(null);
        }
        return this;
    }

    /**
     * 解码Gif图片
     */
    @SuppressWarnings("unused")
    public LoadHelper decodeGifImage() {
        options.setDecodeGifImage(true);
        return this;
    }

    /**
     * 设置最大尺寸，在解码的时候会使用此Size来计算inSimpleSize
     */
    public LoadHelper maxSize(int width, int height) {
        options.setMaxSize(width, height);
        return this;
    }

    /**
     * 裁剪图片，将原始图片加载到内存中之后根据resize进行裁剪。裁剪的原则就是最终返回的图片的比例一定是跟resize一样的，但尺寸不一定会等于resi，也有可能小于resize
     */
    public LoadHelper resize(int width, int height) {
        options.setResize(width, height);
        return this;
    }

    /**
     * 裁剪图片，将原始图片加载到内存中之后根据resize进行裁剪。裁剪的原则就是最终返回的图片的比例一定是跟resize一样的，但尺寸不一定会等于resize，也有可能小于resize，如果需要必须同resize一致可以设置forceUseResize
     */
    public LoadHelper resize(int width, int height, ScaleType scaleType) {
        options.setResize(new Resize(width, height, scaleType));
        return this;
    }

    /**
     * 强制使经过resize处理后的图片同resize的尺寸一致
     */
    public LoadHelper forceUseResize() {
        options.setForceUseResize(true);
        return this;
    }

    /**
     * 返回低质量的图片
     */
    public LoadHelper lowQualityImage() {
        options.setLowQualityImage(true);
        return this;
    }

    /**
     * 设置图片处理器，图片处理器会根据resize创建一张新的图片
     */
    @SuppressWarnings("unused")
    public LoadHelper processor(ImageProcessor processor) {
        options.setImageProcessor(processor);
        return this;
    }

    /**
     * 设置图片质量
     */
    @SuppressWarnings("unused")
    public LoadHelper bitmapConfig(Bitmap.Config config){
        options.setBitmapConfig(config);
        return this;
    }

    /**
     * 批量设置加载参数，这会是一个合并的过程，并不会完全覆盖
     */
    public LoadHelper options(LoadOptions newOptions) {
        options.apply(newOptions);
        return this;
    }

    /**
     * 批量设置加载参数，你只需要提前将LoadOptions通过Sketch.putLoadOptions()方法存起来，然后在这里指定其名称即可，另外这会是一个合并的过程，并不会完全覆盖
     */
    public LoadHelper optionsByName(Enum<?> optionsName) {
        return options(Sketch.getLoadOptions(optionsName));
    }

    /**
     * 设置进度监听器
     */
    @SuppressWarnings("unused")
    public LoadHelper progressListener(DownloadProgressListener downloadProgressListener) {
        this.progressListener = downloadProgressListener;
        return this;
    }

    /**
     * 设置加载监听器
     */
    public LoadHelper listener(LoadListener loadListener) {
        this.listener = loadListener;
        return this;
    }

    /**
     * 对属性进行预处理
     */
    protected void preProcess() {
        Configuration configuration = sketch.getConfiguration();

        // 没有ImageProcessor但有resize的话就需要设置一个默认的图片裁剪处理器
        if (options.getImageProcessor() == null && options.getResize() != null) {
            options.setImageProcessor(configuration.getDefaultCutImageProcessor());
        }

        // 没有设置maxSize的话，就用默认的maxSize
        if (options.getMaxSize() == null) {
            options.setMaxSize(configuration.getImageSizeCalculator().getDefaultImageMaxSize(configuration.getContext()));
        }

        // 如果设置了全局禁止使用磁盘缓存的话就强制关闭磁盘缓存功能
        if (!configuration.isCacheInDisk()) {
            options.setCacheInDisk(false);
        }

        // 如果设置了全局使用低质量图片的话就强制使用低质量的图片
        if (configuration.isLowQualityImage()) {
            options.setLowQualityImage(true);
        }

        // 如果没有设置请求Level的话就跟据暂停下载和暂停加载功能来设置请求Level
        if (options.getRequestLevel() == null) {
            if (configuration.isPauseDownload()) {
                options.setRequestLevel(RequestLevel.LOCAL);
                options.setRequestLevelFrom(RequestLevelFrom.PAUSE_DOWNLOAD);
            }

            // 暂停加载对于加载请求并不起作用，因此这里不予处理
        }

        // 没有设置名称的话就用uri作为名称，名称主要用来在log中区分请求的
        if (attrs.getName() == null) {
            attrs.setName(attrs.getUri());
        }
    }

    /**
     * 提交请求
     *
     * @return LoadRequest 你可以通过Request来查看请求的状态或者取消这个请求
     */
    public LoadRequest commit() {
        if (listener != null) {
            listener.onStarted();
        }

        preProcess();

        if(!checkUri()){
            return null;
        }

        if(!checkUriScheme()){
            return null;
        }

        if(!checkRequestLevel()){
            return null;
        }

        return submitRequest();
    }

    private boolean checkUri(){
        if (attrs.getUri() == null || "".equals(attrs.getUri().trim())) {
            if (Sketch.isDebugMode()) {
                Log.e(Sketch.TAG, SketchUtils.concat(NAME, " - ", "uri is null or empty"));
            }
            if (listener != null) {
                listener.onFailed(FailedCause.URI_NULL_OR_EMPTY);
            }
            return false;
        }

        return true;
    }

    private boolean checkUriScheme(){
        if (attrs.getUriScheme() == null) {
            Log.e(Sketch.TAG, SketchUtils.concat(NAME, " - ", "unknown uri scheme", " - ", attrs.getName()));
            if (listener != null) {
                listener.onFailed(FailedCause.URI_NO_SUPPORT);
            }
            return false;
        }

        return true;
    }

    private boolean checkRequestLevel(){
        // 如果只从本地加载并且是网络请求并且磁盘中没有缓存就结束吧
        if (options.getRequestLevel() == RequestLevel.LOCAL && attrs.getUriScheme() == UriScheme.NET && sketch.getConfiguration().getDiskCache().get(attrs.getUri()) == null) {
            boolean isPauseDownload = options.getRequestLevelFrom() == RequestLevelFrom.PAUSE_DOWNLOAD;

            if (Sketch.isDebugMode()) {
                Log.w(Sketch.TAG, SketchUtils.concat(NAME, " - ", "canceled", " - ", isPauseDownload ? "pause download" : "requestLevel is local", " - ", attrs.getName()));
            }

            if(listener != null){
                listener.onCanceled(isPauseDownload ? CancelCause.PAUSE_DOWNLOAD : CancelCause.LEVEL_IS_LOCAL);
            }

            return false;
        }

        return true;
    }

    private LoadRequest submitRequest(){
        RequestFactory requestFactory = sketch.getConfiguration().getRequestFactory();
        LoadRequest request = requestFactory.newLoadRequest(sketch, attrs, options, listener, progressListener);
        request.submit();
        return request;
    }
}