package org.dawb.passerelle.actors.hardware.image;

import java.net.MalformedURLException;
import java.net.URL;

import org.dawb.passerelle.actors.Activator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

public class ImageUtils {

	

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return getImageDescriptor(Activator.PLUGIN_ID, path);
	}
	public static ImageDescriptor getImageDescriptor(String plugin,String path) {
		return imageDescriptorFromPlugin(plugin, path);
	}

	private static ImageDescriptor imageDescriptorFromPlugin(String pluginId, String imageFilePath) {
		
        if (pluginId == null || imageFilePath == null) {
            throw new IllegalArgumentException();
        }

		IWorkbench workbench = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench() : null;
		ImageDescriptor imageDescriptor = workbench == null ? null : workbench
				.getSharedImages().getImageDescriptor(imageFilePath);
		if (imageDescriptor != null)
			return imageDescriptor; // found in the shared images

        // if the bundle is not ready then there is no image
        Bundle bundle = Platform.getBundle(pluginId);
        if (!BundleUtility.isReady(bundle)) {
			return null;
		}

        // look for the image (this will check both the plugin and fragment folders
        URL fullPathString = BundleUtility.find(bundle, imageFilePath);
        if (fullPathString == null) {
            try {
                fullPathString = new URL(imageFilePath);
            } catch (MalformedURLException e) {
                return null;
            }
			URL platformURL = FileLocator.find(fullPathString);
			if (platformURL != null) {
				fullPathString = platformURL;
			}
        }

        return ImageDescriptor.createFromURL(fullPathString);
    }
}
