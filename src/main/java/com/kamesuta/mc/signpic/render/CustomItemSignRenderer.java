package com.kamesuta.mc.signpic.render;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.kamesuta.mc.signpic.entry.Entry;
import com.kamesuta.mc.signpic.entry.EntryId;
import com.kamesuta.mc.signpic.image.meta.ImageSize;
import com.kamesuta.mc.signpic.image.meta.ImageSize.ImageSizes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IPerspectiveAwareModel;

public class CustomItemSignRenderer implements IPerspectiveAwareModel {
	public static final ModelResourceLocation modelResourceLocation = new ModelResourceLocation("minecraft:sign");
	private final IBakedModel baseModel;
	private ItemStack itemStack;
	private boolean isOverride;

	public CustomItemSignRenderer(final IBakedModel model) {
		this.baseModel = model;
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(final TransformType cameraTransformType) {
		Pair<? extends IBakedModel, Matrix4f> pair = null;
		if (this.baseModel instanceof IPerspectiveAwareModel)
			pair = ((IPerspectiveAwareModel) this.baseModel).handlePerspective(cameraTransformType);
		if (this.itemStack!=null&&this.isOverride) {
			OpenGL.glPushMatrix();
			if (pair!=null&&pair.getRight()!=null)
				ForgeHooksClient.multiplyCurrentGlMatrix(pair.getRight());
			OpenGL.glDisable(GL11.GL_CULL_FACE);
			renderItem(cameraTransformType, this.itemStack);
			OpenGL.glEnable(GL11.GL_LIGHTING);
			OpenGL.glEnable(GL11.GL_BLEND);
			OpenGL.glEnable(GL11.GL_TEXTURE_2D);
			OpenGL.glEnable(GL11.GL_CULL_FACE);
			OpenGL.glPopMatrix();
		}
		if (pair!=null&&!this.isOverride)
			return ((IPerspectiveAwareModel) this.baseModel).handlePerspective(cameraTransformType);
		return Pair.of(this, null);
	}

	public void renderItem(final TransformType type, final ItemStack item) {
		OpenGL.glPushMatrix();
		OpenGL.glPushAttrib();
		OpenGL.glDisable(GL_CULL_FACE);
		final Entry entry = EntryId.fromItemStack(item).entry();
		// Size
		final ImageSize size = new ImageSize().setAspectSize(entry.meta.size, entry.content().image.getSize());
		OpenGL.glScalef(1f, -1f, 1f);
		if (type==TransformType.GUI) {
			final float slot = 1f;
			final ImageSize size2 = new ImageSize().setSize(ImageSizes.INNER, size, slot, slot);
			//OpenGL.glScalef(.5f, .5f, 1f);
			OpenGL.glTranslatef((slot-size2.width)/2f, (slot-size2.height)/2f, 0f);
			OpenGL.glTranslatef(-.5f, -.5f, 0f);
			OpenGL.glScalef(slot, slot, 1f);
			entry.gui.drawScreen(0, 0, 0f, 1f, size2.width/slot, size2.height/slot);
		} else {
			OpenGL.glScalef(2f, 2f, 1f);
			if (type==TransformType.GROUND)
				OpenGL.glTranslatef(-size.width/2f, .25f, 0f);
			else if (type==TransformType.FIXED) {
				final float f = 0.0078125F; // vanilla map offset
				OpenGL.glTranslatef(-size.width/2f, .5f, f);
			} else if (type==TransformType.FIRST_PERSON_LEFT_HAND) {
				OpenGL.glScalef(-1f, 1f, 1f);
				OpenGL.glTranslatef(.25f, .25f, 0f);
				OpenGL.glTranslatef(-size.width, 0f, 0f);
			} else if (type==TransformType.FIRST_PERSON_RIGHT_HAND)
				OpenGL.glTranslatef(-.25f, .25f, 0f);
			else if (type==TransformType.THIRD_PERSON_LEFT_HAND) {
				OpenGL.glTranslatef(.25f, .25f, 0f);
				OpenGL.glTranslatef(-size.width, 0f, 0f);
			} else if (type==TransformType.THIRD_PERSON_RIGHT_HAND)
				OpenGL.glTranslatef(-.25f, .25f, 0f);
			else if (type==TransformType.HEAD)
				OpenGL.glTranslatef(-size.width/2f, .25f, 0f);
			OpenGL.glTranslatef(0f, -size.height, 0f);
			OpenGL.glTranslatef(entry.meta.offset.x, entry.meta.offset.y, entry.meta.offset.z);
			entry.meta.rotation.rotate();
			entry.gui.drawScreen(0, 0, 0f, 1f, size.width, size.height);
		}
		OpenGL.glPopAttrib();
		OpenGL.glPopMatrix();
	}

	@Override
	public boolean isGui3d() {
		return this.baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return this.baseModel.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return this.baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return this.baseModel.isAmbientOcclusion();
	}

	@Override
	public List<BakedQuad> getQuads(final IBlockState state, final EnumFacing side, final long rand) {
		if (this.isOverride)
			return ImmutableList.<BakedQuad> of();
		else
			return this.baseModel.getQuads(state, side, rand);
	}

	private ItemOverrideList overrides = new ItemOverrideList(ImmutableList.<ItemOverride> of()) {
		@Override
		public IBakedModel handleItemState(final IBakedModel originalModel, final ItemStack stack, final World world, final EntityLivingBase entity) {
			CustomItemSignRenderer.this.itemStack = stack;
			CustomItemSignRenderer.this.isOverride = stack!=null&&stack.getItem()==Items.SIGN&&EntryId.fromItemStack(stack).entry().isValid();
			return CustomItemSignRenderer.this.baseModel.getOverrides().handleItemState(originalModel, stack, world, entity);
		}
	};

	@Override
	public ItemOverrideList getOverrides() {
		return this.overrides;
	}
}