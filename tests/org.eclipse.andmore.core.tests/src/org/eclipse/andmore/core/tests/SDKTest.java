package org.eclipse.andmore.core.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.andmore.core.internal.AndroidSDKService;
import org.eclipse.andmore.core.sdk.IAndroidSDKService;
import org.junit.Test;

@SuppressWarnings("nls")
public class SDKTest {

	@Test
	public void testGetProps() throws IOException {
		IAndroidSDKService service = new AndroidSDKService();
		Collection<String> devices = service.getDevices();
		Map<String, String> props = service.getProperties(devices.iterator().next());
		assertTrue(!props.isEmpty());
	}

	@Test
	public void testGetProp() throws IOException {
		IAndroidSDKService service = new AndroidSDKService();
		Collection<String> devices = service.getDevices();
		String value = service.getProperty(devices.iterator().next(), "ro.product.publicname");
		assertNotNull(value);
	}

}
