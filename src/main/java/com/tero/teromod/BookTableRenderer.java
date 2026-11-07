package com.tero.teromod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BookTableRenderer implements BlockEntityRenderer<BookTableEntity, BookTableRenderState> {
    private static final SpriteId BOOK_TEXTURE =
            Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("enchantment/enchanting_table_book");

    private final SpriteGetter sprites;
    private final BookModel bookModel;

    public BookTableRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public BookTableRenderState createRenderState() {
        return new BookTableRenderState();
    }

    @Override
    public void extractRenderState(BookTableEntity blockEntity, BookTableRenderState state, float partialTicks,
                                   Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.time = blockEntity.getLevel() == null
                ? partialTicks
                : blockEntity.getLevel().getGameTime() + partialTicks;
    }

    @Override
    public void submit(BookTableRenderState state, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.12F + Mth.sin(state.time * 0.08F) * 0.025F, 0.5F);
        poseStack.rotate(Axis.YP, state.time * 0.012F);
        poseStack.rotateDegrees(Axis.ZP, 80.0F);

        float page1 = Mth.frac(state.time * 0.015F + 0.25F) * 1.6F - 0.3F;
        float page2 = Mth.frac(state.time * 0.015F + 0.75F) * 1.6F - 0.3F;
        BookModel.State bookState = BookModel.State.forAnimation(
                state.time,
                Mth.clamp(page1, 0.0F, 1.0F),
                Mth.clamp(page2, 0.0F, 1.0F),
                1.0F
        );

        submitNodeCollector.submitModel(
                this.bookModel,
                bookState,
                poseStack,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                BOOK_TEXTURE,
                this.sprites,
                0
        );
        if (state.breakProgress != null) {
            submitNodeCollector.order(1).submitCrumblingOverlay(
                    this.bookModel,
                    bookState,
                    poseStack,
                    BOOK_TEXTURE.renderType(this.bookModel.renderType()),
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    -1,
                    state.breakProgress
            );
        }
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(BookTableEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(0.0, 1.0, 0.0);
    }
}
