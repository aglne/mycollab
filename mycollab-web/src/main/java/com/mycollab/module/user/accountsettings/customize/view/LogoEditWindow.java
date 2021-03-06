/**
 * Copyright © MyCollab
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycollab.module.user.accountsettings.customize.view;

import com.mycollab.common.i18n.FileI18nEnum;
import com.mycollab.common.i18n.GenericI18Enum;
import com.mycollab.common.i18n.ShellI18nEnum;
import com.mycollab.core.MyCollabException;
import com.mycollab.core.UserInvalidInputException;
import com.mycollab.core.utils.ImageUtil;
import com.mycollab.module.file.service.AccountLogoService;
import com.mycollab.module.user.accountsettings.view.event.SettingEvent;
import com.mycollab.spring.AppContextUtil;
import com.mycollab.vaadin.AppUI;
import com.mycollab.vaadin.EventBusFactory;
import com.mycollab.vaadin.UserUIContext;
import com.mycollab.vaadin.ui.AccountAssetsResolver;
import com.mycollab.vaadin.ui.ELabel;
import com.mycollab.vaadin.ui.UIUtils;
import com.mycollab.vaadin.web.ui.ByteArrayImageResource;
import com.mycollab.vaadin.web.ui.WebThemes;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.cropper.Cropper;
import org.vaadin.cropper.client.CropSelection;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;
import org.vaadin.viritin.layouts.MWindow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author MyCollab Ltd.
 * @since 4.1
 */
class LogoEditWindow extends MWindow {
    private static final long serialVersionUID = -5294741083557671011L;
    private static final Logger LOG = LoggerFactory.getLogger(LogoEditWindow.class);

    private MVerticalLayout content;

    private BufferedImage originalImage;
    private Embedded previewImage;
    private byte[] scaleImageData;

    LogoEditWindow(byte[] imageData) {
        super(UserUIContext.getMessage(FileI18nEnum.ACTION_CHANGE_LOGO));
        this.withModal(true).withResizable(false).withWidth("800px").withHeight("800px");
        content = new MVerticalLayout();
        this.setContent(content);
        editPhoto(imageData);
    }

    private void editPhoto(byte[] imageData) {
        try {
            originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new UserInvalidInputException("Invalid image type");
        }
        originalImage = ImageUtil.scaleImage(originalImage, 650, 650);

        MHorizontalLayout previewBox = new MHorizontalLayout().withMargin(new MarginInfo(false, true, true, false))
                .withFullWidth();

        final String logoPath = AppUI.getBillingAccount().getLogopath();
        Resource defaultPhoto = AccountAssetsResolver.createLogoResource(logoPath, 150);
        previewImage = new Embedded(null, defaultPhoto);
        previewImage.setWidth("100px");
        previewBox.addComponent(previewImage);
        previewBox.setComponentAlignment(previewImage, Alignment.TOP_LEFT);

        MVerticalLayout previewBoxRight = new MVerticalLayout().withSpacing(false).withMargin(new MarginInfo(false, true, false, true));

        previewBoxRight.addComponent(ELabel.html(UserUIContext.getMessage(ShellI18nEnum.OPT_IMAGE_EDIT_INSTRUCTION)));

        MButton cancelBtn = new MButton(UserUIContext.getMessage(GenericI18Enum.BUTTON_CANCEL),
                clickEvent -> EventBusFactory.getInstance().post(new SettingEvent.GotoGeneralSetting(LogoEditWindow.this, null)))
                .withStyleName(WebThemes.BUTTON_OPTION);

        MButton acceptBtn = new MButton(UserUIContext.getMessage(GenericI18Enum.BUTTON_ACCEPT), clickEvent -> {
            if (scaleImageData != null && scaleImageData.length > 0) {
                try {
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(scaleImageData));
                    AccountLogoService accountLogoService = AppContextUtil.getSpringBean(AccountLogoService.class);
                    accountLogoService.upload(UserUIContext.getUsername(),
                            image, AppUI.getAccountId());
                    UIUtils.reloadPage();
                } catch (IOException e) {
                    throw new MyCollabException("Error when saving account logo", e);
                }
            }
        }).withStyleName(WebThemes.BUTTON_ACTION).withIcon(VaadinIcons.CLIPBOARD).withClickShortcut(ShortcutAction.KeyCode.ENTER);

        MHorizontalLayout controlBtns = new MHorizontalLayout(acceptBtn, cancelBtn);
        previewBoxRight.with(controlBtns).withAlign(controlBtns, Alignment.TOP_LEFT);
        previewBox.with(previewBoxRight).expand(previewBoxRight);
        content.addComponent(previewBox);

        CssLayout cropBox = new CssLayout();
        cropBox.setWidth("100%");
        VerticalLayout currentPhotoBox = new VerticalLayout();
        Resource resource = new ByteArrayImageResource(
                ImageUtil.convertImageToByteArray(originalImage), "image/png");
        Cropper cropField = new Cropper(resource);
        cropField.setAspectRatio(150 / 28);
        cropField.addCropSelectionChangedListener(valueChangeEvent -> {
            CropSelection newSelection = valueChangeEvent.getSelection();
            int x1 = newSelection.getX();
            int y1 = newSelection.getY();
            int x2 = newSelection.getWidth();
            int y2 = newSelection.getHeight();
            if (x2 > x1 && y2 > y1) {
                BufferedImage subImage = originalImage.getSubimage(x1, y1, x2, y2);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                try {
                    ImageIO.write(subImage, "png", outStream);
                    scaleImageData = outStream.toByteArray();
                    displayPreviewImage();
                } catch (IOException e) {
                    LOG.error("Error while scale image: ", e);
                }
            }
        });
        currentPhotoBox.setWidth("650px");
        currentPhotoBox.setHeight("650px");
        currentPhotoBox.addComponent(cropField);
        cropBox.addComponent(currentPhotoBox);

        content.with(previewBox, ELabel.hr(), cropBox);
    }

    private void displayPreviewImage() {
        if (scaleImageData != null && scaleImageData.length > 0) {
            ByteArrayImageResource previewResource = new ByteArrayImageResource(scaleImageData, "image/png");
            previewImage.setSource(previewResource);
        }
    }
}
