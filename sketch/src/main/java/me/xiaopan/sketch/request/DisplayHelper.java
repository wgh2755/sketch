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
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;

import me.xiaopan.sketch.Sketch;
import me.xiaopan.sketch.display.ImageDisplayer;
import me.xiaopan.sketch.display.TransitionImageDisplayer;
import me.xiaopan.sketch.drawable.BindFixedRecycleBitmapDrawable;
import me.xiaopan.sketch.drawable.RecycleDrawable;
import me.xiaopan.sketch.feture.RequestFactory;
import me.xiaopan.sketch.process.ImageProcessor;
import me.xiaopan.sketch.util.SketchUtils;

public class DisplayHelper {
    protected static final String NAME = "DisplayHelper";

    protected Sketch sketch;

    protected RequestAttrs requestAttrs = new RequestAttrs();
    protected DisplayAttrs displayAttrs = new DisplayAttrs();
    protected DisplayOptions displayOptions = new DisplayOptions();
    protected DisplayListener displayListener;
    protected DownloadProgressListener progressListener;
    protected ImageViewInterface imageViewInterface;

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
    public DisplayHelper(Sketch sketch, String uri, ImageViewInterface imageViewInterface) {
        init(sketch, uri, imageViewInterface);
    }

    public DisplayHelper(Sketch sketch, DisplayParams displayParams, ImageViewInterface imageViewInterface) {
        init(sketch, displayParams, imageViewInterface);
    }

    /**
     * 初始化
     */
    public DisplayHelper init(Sketch sketch, String uri, ImageViewInterface imageViewInterface) {
        this.sketch = sketch;
        this.imageViewInterface = imageViewInterface;

        requestAttrs.reset(uri);
        displayAttrs.reset(imageViewInterface, sketch);
        displayOptions.copy(imageViewInterface.getOptions());

        return this;
    }

    /**
     * 初始化，此方法用来在RecyclerView中恢复使用
     */
    public DisplayHelper init(Sketch sketch, DisplayParams params, ImageViewInterface imageViewInterface) {
        this.sketch = sketch;
        this.imageViewInterface = imageViewInterface;

        requestAttrs.copy(params.attrs);
        displayAttrs.reset(imageViewInterface, sketch);
        displayOptions.copy(params.options);

        return this;
    }

    /**
     * 重置所有属性
     */
    public void reset() {
        sketch = null;

        requestAttrs.reset(null);
        displayOptions.reset();
        displayListener = null;
        progressListener = null;
        displayAttrs.reset(null, null);
        imageViewInterface = null;
    }

    /**
     * 设置名称，用于在log总区分请求
     */
    public DisplayHelper name(String name) {
        this.requestAttrs.setName(name);
        return this;
    }

    /**
     * 关闭硬盘缓存
     */
    @SuppressWarnings("unused")
    public DisplayHelper disableDiskCache() {
        displayOptions.setCacheInDisk(false);
        return this;
    }

    /**
     * 设置请求Level
     */
    public DisplayHelper requestLevel(RequestLevel requestLevel) {
        if (requestLevel != null) {
            displayOptions.setRequestLevel(requestLevel);
            displayOptions.setRequestLevelFrom(null);
        }
        return this;
    }

    /**
     * 解码Gif图片
     */
    @SuppressWarnings("unused")
    public DisplayHelper decodeGifImage() {
        displayOptions.setDecodeGifImage(true);
        return this;
    }

    /**
     * 设置最大尺寸，在解码时会使用此Size来计算inSimpleSize
     */
    public DisplayHelper maxSize(int width, int height) {
        displayOptions.setMaxSize(width, height);
        return this;
    }

    /**
     * 裁剪图片，将原始图片加载到内存中之后根据resize进行裁剪。裁剪的原则就是最终返回的图片的比例一定是跟resize一样的，但尺寸不一定会等于resize，也有可能小于resize
     */
    public DisplayHelper resize(int width, int height) {
        displayOptions.setResize(width, height);
        return this;
    }

    /**
     * 裁剪图片，将原始图片加载到内存中之后根据resize进行裁剪。裁剪的原则就是最终返回的图片的比例一定是跟resize一样的，但尺寸不一定会等于resize，也有可能小于resize，如果需要必须同resize一致可以设置forceUseResize
     */
    public DisplayHelper resize(int width, int height, ScaleType scaleType) {
        displayOptions.setResize(new Resize(width, height, scaleType));
        return this;
    }

    /**
     * 使用ImageView的layout_width和layout_height作为resize
     */
    @SuppressWarnings("unused")
    public DisplayHelper resizeByFixedSize() {
        displayOptions.setResizeByFixedSize(true);
        return this;
    }

    /**
     * 强制使经过resize处理后的图片同resize的尺寸一致
     */
    public DisplayHelper forceUseResize() {
        displayOptions.setForceUseResize(true);
        return this;
    }

    /**
     * 返回低质量的图片
     */
    public DisplayHelper lowQualityImage() {
        displayOptions.setLowQualityImage(true);
        return this;
    }

    /**
     * 设置图片处理器，图片处理器会根据resize和ScaleType创建一张新的图片
     */
    @SuppressWarnings("unused")
    public DisplayHelper processor(ImageProcessor processor) {
        displayOptions.setImageProcessor(processor);
        return this;
    }

    /**
     * 设置图片质量
     */
    @SuppressWarnings("unused")
    public DisplayHelper bitmapConfig(Bitmap.Config config){
        displayOptions.setBitmapConfig(config);
        return this;
    }

    /**
     * 关闭内存缓存
     */
    @SuppressWarnings("unused")
    public DisplayHelper disableMemoryCache() {
        displayOptions.setCacheInMemory(false);
        return this;
    }

    /**
     * 设置图片显示器，在加载完成后会调用此显示器来显示图片
     */
    @SuppressWarnings("unused")
    public DisplayHelper displayer(ImageDisplayer displayer) {
        displayOptions.setImageDisplayer(displayer);
        return this;
    }

    /**
     * 设置内存缓存ID（大多数情况下你不需要手动设置缓存ID，除非你想使用通过putBitmap()放到缓存中的图片）
     */
    @SuppressWarnings("unused")
    public DisplayHelper memoryCacheId(String memoryCacheId) {
        this.displayAttrs.setMemoryCacheId(memoryCacheId);
        return this;
    }

    /**
     * 设置正在加载时显示的图片
     */
    public DisplayHelper loadingImage(ImageHolder loadingImageHolder) {
        displayOptions.setLoadingImage(loadingImageHolder);
        return this;
    }

    /**
     * 设置正在加载时显示的图片
     */
    @SuppressWarnings("unused")
    public DisplayHelper loadingImage(int drawableResId) {
        loadingImage(new ImageHolder(drawableResId));
        return this;
    }

    /**
     * 设置失败时显示的图片
     */
    public DisplayHelper failedImage(ImageHolder failedImageHolder) {
        displayOptions.setFailedImage(failedImageHolder);
        return this;
    }

    /**
     * 设置失败时显示的图片
     */
    @SuppressWarnings("unused")
    public DisplayHelper failedImage(int drawableResId) {
        failedImage(new ImageHolder(drawableResId));
        return this;
    }

    /**
     * 设置暂停下载时显示的图片
     */
    public DisplayHelper pauseDownloadImage(ImageHolder pauseDownloadImageHolder) {
        displayOptions.setPauseDownloadImage(pauseDownloadImageHolder);
        return this;
    }

    /**
     * 设置暂停下载时显示的图片
     */
    @SuppressWarnings("unused")
    public DisplayHelper pauseDownloadImage(int drawableResId) {
        pauseDownloadImage(new ImageHolder(drawableResId));
        return this;
    }

    /**
     * 批量设置显示参数，这会是一个合并的过程，并不会完全覆盖
     */
    public DisplayHelper options(DisplayOptions newOptions) {
        displayOptions.apply(newOptions);
        return this;
    }

    /**
     * 批量设置显示参数，你只需要提前将DisplayOptions通过Sketch.putDisplayOptions()方法存起来，然后在这里指定其名称即可，另外这会是一个合并的过程，并不会完全覆盖
     */
    @SuppressWarnings("unused")
    public DisplayHelper optionsByName(Enum<?> optionsName) {
        return options(Sketch.getDisplayOptions(optionsName));
    }

    /**
     * 提交请求
     *
     * @return DisplayRequest 你可以通过Request来查看请求的状态或者取消这个请求
     */
    public DisplayRequest commit() {
        if (displayListener != null) {
            displayListener.onStarted();
        }

        saveParams();
        preProcess();

        if(!checkUri()){
            sketch.getConfiguration().getHelperFactory().recycleDisplayHelper(this);
            return null;
        }

        if(!checkUriScheme()){
            sketch.getConfiguration().getHelperFactory().recycleDisplayHelper(this);
            return null;
        }

        if(!checkMemoryCache()){
            sketch.getConfiguration().getHelperFactory().recycleDisplayHelper(this);
            return null;
        }

        if(!checkRequestLevel()){
            sketch.getConfiguration().getHelperFactory().recycleDisplayHelper(this);
            return null;
        }

        DisplayRequest potentialRequest = checkRepeatRequest();
        if(potentialRequest != null){
            sketch.getConfiguration().getHelperFactory().recycleDisplayHelper(this);
            return potentialRequest;
        }

        DisplayRequest request = submitRequest();
        sketch.getConfiguration().getHelperFactory().recycleDisplayHelper(this);
        return request;
    }

    /**
     * 将相关信息保存在SketchImageView中，以便在RecyclerView中恢复显示使用
     */
    private void saveParams() {
        DisplayParams displayParams = imageViewInterface.getDisplayParams();
        if (displayParams == null) {
            displayParams = new DisplayParams();
            imageViewInterface.setDisplayParams(displayParams);
        }

        displayParams.attrs.copy(requestAttrs);
        displayParams.options.copy(displayOptions);
    }

    protected void preProcess() {
        // 根据ImageVie的固定大小计算resize
        if (displayOptions.isResizeByFixedSize()) {
            displayOptions.setResize(sketch.getConfiguration().getImageSizeCalculator().calculateImageResize(imageViewInterface));
        }

        // 如果没有设置ScaleType的话就从ImageView身上取
        if (displayOptions.getResize() != null && displayOptions.getResize().getScaleType() == null && imageViewInterface != null) {
            displayOptions.getResize().setScaleType(displayAttrs.getScaleType());
        }

        // 没有ImageProcessor但有resize的话就需要设置一个默认的图片裁剪处理器
        if (displayOptions.getImageProcessor() == null && displayOptions.getResize() != null) {
            displayOptions.setImageProcessor(sketch.getConfiguration().getDefaultCutImageProcessor());
        }

        // 没有设置maxSize的话，如果ImageView的宽高是的固定的就根据ImageView的宽高来作为maxSize，否则就用默认的maxSize
        if (displayOptions.getMaxSize() == null) {
            MaxSize maxSize = sketch.getConfiguration().getImageSizeCalculator().calculateImageMaxSize(imageViewInterface);
            if (maxSize == null) {
                maxSize = sketch.getConfiguration().getImageSizeCalculator().getDefaultImageMaxSize(sketch.getConfiguration().getContext());
            }
            displayOptions.setMaxSize(maxSize);
        }

        // 如果设置了全局禁止使用磁盘缓存的话就强制关闭磁盘缓存功能
        if (!sketch.getConfiguration().isCacheInDisk()) {
            displayOptions.setCacheInDisk(false);
        }

        // 如果设置了全局禁止使用内存缓存的话就强制内存磁盘缓存功能
        if (!sketch.getConfiguration().isCacheInMemory()) {
            displayOptions.setCacheInMemory(false);
        }

        // 如果设置了全局使用低质量图片的话就强制使用低质量的图片
        if (sketch.getConfiguration().isLowQualityImage()) {
            displayOptions.setLowQualityImage(true);
        }

        // 如果没有设置请求Level的话就跟据暂停下载和暂停加载功能来设置请求Level
        if (displayOptions.getRequestLevel() == null) {
            if (sketch.getConfiguration().isPauseDownload()) {
                displayOptions.setRequestLevel(RequestLevel.LOCAL);
                displayOptions.setRequestLevelFrom(RequestLevelFrom.PAUSE_DOWNLOAD);
            }

            if (sketch.getConfiguration().isPauseLoad()) {
                displayOptions.setRequestLevel(RequestLevel.MEMORY);
                displayOptions.setRequestLevelFrom(RequestLevelFrom.PAUSE_LOAD);
            }
        }

        // ImageDisplayer必须得有
        if (displayOptions.getImageDisplayer() == null) {
            displayOptions.setImageDisplayer(sketch.getConfiguration().getDefaultImageDisplayer());
        }

        // 使用过渡图片显示器的时候，如果使用了loadingImage的话ImageView就必须采用固定宽高以及ScaleType必须是CENTER_CROP
        if (displayOptions.getImageDisplayer() instanceof TransitionImageDisplayer
                && displayOptions.getLoadingImageHolder() != null
                && (displayAttrs.getFixedSize() == null || displayAttrs.getScaleType() != ScaleType.CENTER_CROP)) {
            View imageView = imageViewInterface.getSelf();
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            String errorInfo = SketchUtils.concat(
                    "If you use TransitionImageDisplayer and loadingImage, ",
                    "ImageView width and height must be fixed as well as the ScaleType must be CENTER_CROP. ",
                    "Now ",
                    " width is ", SketchUtils.viewLayoutFormatted(layoutParams.width),
                    ", height is ", SketchUtils.viewLayoutFormatted(layoutParams.height),
                    ", ScaleType is ", displayAttrs.getScaleType().name());
            if (Sketch.isDebugMode()) {
                Log.d(Sketch.TAG, SketchUtils.concat(NAME, " - ", errorInfo, " - ", requestAttrs.getUri()));
            }
            throw new IllegalArgumentException(errorInfo);
        }

        // 没有设置内存缓存ID的话就计算内存缓存ID，这个通常是不是需要使用者主动设置的，除非你想使用你自己放入MemoryCache中的图片
        if (displayAttrs.getMemoryCacheId() == null) {
            displayAttrs.setMemoryCacheId(displayOptions.appendMemoryCacheKey(new StringBuilder().append(requestAttrs.getUri())).toString());
        }

        // 没有设置名称的话就用内存缓存ID作为名称，名称主要用来在log中区分请求的
        if (requestAttrs.getName() == null) {
            requestAttrs.setName(displayAttrs.getMemoryCacheId());
        }

        // onDisplay一定要放在getDisplayListener()和getProgressListener()之前调用，因为在onDisplay的时候会设置一些属性，这些属性会影响到getDisplayListener()和getProgressListener()的结果
        imageViewInterface.onDisplay();

        displayListener = imageViewInterface.getDisplayListener();
        progressListener = imageViewInterface.getDownloadProgressListener();
    }

    private boolean checkUri(){
        if (requestAttrs.getUri() == null || "".equals(requestAttrs.getUri().trim())) {
            if (Sketch.isDebugMode()) {
                Log.e(Sketch.TAG, SketchUtils.concat(NAME, " - ", "uri is null or empty"));
            }
            if (displayOptions.getFailedImage() != null) {
                Drawable failedDrawable = displayOptions.getFailedImage().getDrawable(sketch.getConfiguration().getContext(), displayOptions.getImageDisplayer(), displayAttrs.getFixedSize(), displayAttrs.getScaleType());
                imageViewInterface.setImageDrawable(failedDrawable);
            }
            if (displayListener != null) {
                displayListener.onFailed(FailedCause.URI_NULL_OR_EMPTY);
            }
            return false;
        }

        return true;
    }

    private boolean checkUriScheme(){
        if (requestAttrs.getUriScheme() == null) {
            Log.e(Sketch.TAG, SketchUtils.concat(NAME, " - ", "unknown uri scheme: ", requestAttrs.getUri(), " - ", requestAttrs.getName()));
            if (displayOptions.getFailedImage() != null) {
                Drawable failedDrawable = displayOptions.getFailedImage().getDrawable(sketch.getConfiguration().getContext(), displayOptions.getImageDisplayer(), displayAttrs.getFixedSize(), displayAttrs.getScaleType());
                imageViewInterface.setImageDrawable(failedDrawable);
            }
            if (displayListener != null) {
                displayListener.onFailed(FailedCause.URI_NO_SUPPORT);
            }
            return false;
        }

        return true;
    }

    private boolean checkMemoryCache() {
        if (displayOptions.isCacheInMemory()) {
            Drawable cacheDrawable = sketch.getConfiguration().getMemoryCache().get(displayAttrs.getMemoryCacheId());
            if (cacheDrawable != null) {
                RecycleDrawable recycleDrawable = (RecycleDrawable) cacheDrawable;
                if (!recycleDrawable.isRecycled()) {
                    if (Sketch.isDebugMode()) {
                        Log.i(Sketch.TAG, SketchUtils.concat(NAME, " - ", "from memory get bitmap", " - ", recycleDrawable.getInfo(), " - ", requestAttrs.getName()));
                    }
                    imageViewInterface.setImageDrawable(cacheDrawable);
                    if (displayListener != null) {
                        displayListener.onCompleted(ImageFrom.MEMORY_CACHE, recycleDrawable.getMimeType());
                    }
                    return false;
                } else {
                    sketch.getConfiguration().getMemoryCache().remove(displayAttrs.getMemoryCacheId());
                    if (Sketch.isDebugMode()) {
                        Log.e(Sketch.TAG, SketchUtils.concat(NAME, " - ", "memory cache drawable recycled", " - ", recycleDrawable.getInfo(), " - ", requestAttrs.getName()));
                    }
                }
            }
        }

        return true;
    }

    private boolean checkRequestLevel(){
        // 如果已经暂停加载的话就不再从本地或网络加载了
        if (displayOptions.getRequestLevel() == RequestLevel.MEMORY) {
            boolean isPauseLoad = displayOptions.getRequestLevelFrom() == RequestLevelFrom.PAUSE_LOAD;

            if (Sketch.isDebugMode()) {
                Log.w(Sketch.TAG, SketchUtils.concat(NAME, " - ", "canceled", " - ", isPauseLoad ? "pause load" : "requestLevel is memory", " - ", requestAttrs.getName()));
            }

            Drawable loadingDrawable = null;
            if(displayOptions.getLoadingImageHolder() != null){
                loadingDrawable = displayOptions.getLoadingImageHolder().getDrawable(sketch.getConfiguration().getContext(), displayOptions.getImageDisplayer(), displayAttrs.getFixedSize(), displayAttrs.getScaleType());
            }
            imageViewInterface.clearAnimation();
            imageViewInterface.setImageDrawable(loadingDrawable);

            if (displayListener != null) {
                displayListener.onCanceled(isPauseLoad ? CancelCause.PAUSE_LOAD : CancelCause.LEVEL_IS_MEMORY);
            }

            return false;
        }

        // 如果只从本地加载并且是网络请求并且磁盘中没有缓存就结束吧
        if (displayOptions.getRequestLevel() == RequestLevel.LOCAL && requestAttrs.getUriScheme() == UriScheme.NET && sketch.getConfiguration().getDiskCache().get(requestAttrs.getUri()) == null) {
            boolean isPauseDownload = displayOptions.getRequestLevelFrom() == RequestLevelFrom.PAUSE_DOWNLOAD;

            if (Sketch.isDebugMode()) {
                Log.d(Sketch.TAG, SketchUtils.concat(NAME, " - ", "canceled", " - ", isPauseDownload ? "pause download" : "requestLevel is local", " - ", requestAttrs.getName()));
            }

            // 显示暂停下载图片
            if (displayOptions.getPauseDownloadImage() != null) {
                Drawable pauseDownloadDrawable = displayOptions.getPauseDownloadImage().getDrawable(sketch.getConfiguration().getContext(), displayOptions.getImageDisplayer(), displayAttrs.getFixedSize(), displayAttrs.getScaleType());
                imageViewInterface.clearAnimation();
                imageViewInterface.setImageDrawable(pauseDownloadDrawable);
            } else {
                if (Sketch.isDebugMode()) {
                    Log.w(Sketch.TAG, SketchUtils.concat(NAME, " - ", "pauseDownloadDrawable is null", " - ", requestAttrs.getName()));
                }
            }

            if (displayListener != null) {
                displayListener.onCanceled(isPauseDownload ? CancelCause.PAUSE_DOWNLOAD : CancelCause.LEVEL_IS_LOCAL);
            }

            return false;
        }

        return true;
    }

    /**
     * 试图取消已经存在的请求
     * @return DisplayRequest 非null：请求一模一样，无需取消；null：已经取消或没有已存在的请求
     */
    private DisplayRequest checkRepeatRequest(){
        DisplayRequest potentialRequest = BindFixedRecycleBitmapDrawable.findDisplayRequest(imageViewInterface);
        if (potentialRequest != null && !potentialRequest.isFinished()) {
            if (displayAttrs.getMemoryCacheId().equals(potentialRequest.getDisplayAttrs().getMemoryCacheId())) {
                if (Sketch.isDebugMode()) {
                    Log.d(Sketch.TAG, SketchUtils.concat(NAME, " - ", "don't need to cancel", "；", "ImageViewCode", "=", Integer.toHexString(imageViewInterface.hashCode()), "；", potentialRequest.getAttrs().getName()));
                }
                return potentialRequest;
            } else {
                potentialRequest.cancel();
            }
        }

        return null;
    }

    private DisplayRequest submitRequest(){
        RequestFactory requestFactory = sketch.getConfiguration().getRequestFactory();
        DisplayBinder displayBinder = new DisplayBinder(imageViewInterface);
        DisplayRequest request = requestFactory.newDisplayRequest(
                sketch, requestAttrs, displayAttrs, displayOptions,
                displayBinder, displayListener, progressListener);

        // 显示默认图片
        Drawable loadingBindDrawable;
        if (displayOptions.getLoadingImageHolder() != null) {
            loadingBindDrawable = displayOptions.getLoadingImageHolder().getBindDrawable(request);
        } else {
            loadingBindDrawable = new BindFixedRecycleBitmapDrawable(null, request);
        }
        imageViewInterface.setImageDrawable(loadingBindDrawable);

        request.submit();
        return request;
    }
}