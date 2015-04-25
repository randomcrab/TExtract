/*******************************************************************************
 * Copyright (C) 2014-2015 Anton Gustafsson
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.github.antag99.textract.extract;

import java.io.File;
import java.util.Map.Entry;

public final class Steam {
	private Steam() {
	}

	public static File findTerrariaDirectory() {
		try {
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				// Check the windows registry for steam installation path
				try {
					String steamPath = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER,
							"Software\\Valve\\Steam", "SteamPath");
					File result = seekSteamDirectory(new File(steamPath));
					if (result != null) {
						return result;
					}
				} catch (Throwable ignored) {
				}
			}

			// Try to find steam parent directories
			for (File root : File.listRoots()) {
				// Search inside program & 'game' directories
				for (File rootChild : root.listFiles()) {
					if (rootChild.getName().toLowerCase().contains("program") ||
							rootChild.getName().toLowerCase().contains("game")) {
						File result = seekSteamParent(rootChild);
						if (result != null) {
							return result;
						}
					}
				}

				// Try to find steam directory inside root
				File result = seekSteamParent(root);
				if (result != null) {
					return result;
				}
			}

			// Try to find relevant environment variables
			for (Entry<String, String> environmentVariable : System.getenv().entrySet()) {
				if (environmentVariable.getKey().toLowerCase().contains("terraria") |
						environmentVariable.getKey().toLowerCase().contains("tapi")) {

					File result = seekTerrariaDirectory(new File(environmentVariable.getValue()));
					if (result != null) {
						return result;
					}
				} else if (environmentVariable.getKey().toLowerCase().contains("steam")) {
					File result = seekSteamDirectory(new File(environmentVariable.getValue()));
					if (result != null) {
						return result;
					}
				}
			}
		} catch (Throwable ex) {
			// Do not fail because of an exception, but log the error
			ex.printStackTrace();
		}

		// If nothing other works, then prompt the user
		return null;
	}

	private static File seekSteamParent(File parent) {
		if (parent == null || !parent.isDirectory()) {
			return null;
		}

		File[] parentFiles = parent.listFiles();

		if (parentFiles == null) {
			return null;
		}

		for (File child : parentFiles) {
			if (child.getName().toLowerCase().contains("steam")) {
				File result = seekSteamDirectory(child);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	private static File seekSteamDirectory(File steamDirectory) {
		if (steamDirectory == null || !steamDirectory.isDirectory()) {
			return null;
		}

		File steamApps = new File(steamDirectory, "SteamApps");
		File common = new File(steamApps, "Common");

		// We might've ended up inside a SteamApps directory
		if (!steamApps.exists()) {
			common = new File(steamDirectory, "Common");
		}

		File terraria = new File(common, "Terraria");

		return seekTerrariaDirectory(terraria);
	}

	private static File seekTerrariaDirectory(File terrariaDirectory) {
		if (terrariaDirectory == null || !terrariaDirectory.isDirectory()) {
			return null;
		}

		File contentDirectory = new File(terrariaDirectory, "Content");

		if (contentDirectory.exists()) {
			return terrariaDirectory;
		}

		return null;
	}
}
