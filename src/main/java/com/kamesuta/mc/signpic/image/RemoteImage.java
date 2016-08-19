package com.kamesuta.mc.signpic.image;

import java.io.File;
import java.io.IOException;

import com.kamesuta.mc.signpic.Reference;
import com.kamesuta.mc.signpic.image.exception.InvaildImageException;

import net.minecraft.client.resources.I18n;

public class RemoteImage extends Image {
	protected ImageDownloader downloading;
	protected Thread downloadingprocess;
	protected ImageLoader loading;
	protected Thread loadingprocess;
	protected File local;

	public RemoteImage(final String id, final ImageLocation location) {
		super(id);
		this.location = location;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public ImageTextures getTexture() {
		return this.texture;
	}

	@Override
	public ImageState getState() {
		return this.state;
	}

	@Override
	public void init() {
		try {
			Reference.logger.info("PreLoading Start: " + this);
			final File local = this.location.localLocation(this);

			Reference.logger.info("PreLoading/Downloading Start: " + this);
			this.state = ImageState.LOADING;
			if (!local.exists()) {
				Reference.logger.info("File not exists: " + this);
				if (this.downloading == null)
					this.downloading = new ImageDownloader(this, this.location);
				if (this.downloadingprocess == null) {
					this.downloadingprocess = new Thread(this.downloading);
					this.downloadingprocess.start();
				}
			}
		} catch (final Exception e) {
			this.state = ImageState.ERROR;
			this.advmsg = I18n.format("signpic.advmsg.unknown", e);
			Reference.logger.error("UnknownError", e);
		}
	}

	@Override
	public void preload() {
		try {
			final File local = this.location.localLocation(this);
			if (local.exists()) {
				this.local = local;
				this.texture = new ImageTextures(local);
				Reference.logger.info("Loaded: " + this);
				this.state = ImageState.AVAILABLE;
			}
		} catch (final InvaildImageException e) {
			this.state = ImageState.ERROR;
			this.advmsg = I18n.format("signpic.advmsg.invaildimage");
		} catch (final IOException e) {
			this.state = ImageState.ERROR;
			this.advmsg = I18n.format("signpic.advmsg.io", e);
		} catch (final Exception e) {
			this.state = ImageState.ERROR;
			this.advmsg = I18n.format("signpic.advmsg.unknown", e);
			Reference.logger.error("UnknownError", e);
		}
	}

	@Override
	public void load() {

	}

	@Override
	public float getProgress() {
		switch(this.state) {
		case AVAILABLE:
			return 1f;
		case DOWNLOADING:
		case FAILED:
			if (this.downloading != null)
				return this.downloading.getProgress();
		default:
			return 0;
		}
	}

	@Override
	public String getStatusMessage() {
		return I18n.format(this.state.msg, (int)getProgress()*100, this.advmsg);
	}

	@Override
	public String advMessage() {
		return this.advmsg;
	}

	@Override
	public String getLocal() {
		if (this.local != null)
			return "File:"+this.local.getName();
		else
			return "None";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RemoteImage))
			return false;
		final RemoteImage other = (RemoteImage) obj;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Image[%s]", this.id);
	}
}
