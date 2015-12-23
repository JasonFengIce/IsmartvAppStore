package com.boxmate.tv.util;

import java.io.File;
import java.io.FilenameFilter;

public class FileNameFilterCustom implements FilenameFilter{

		public String dat;

		public String getDat() {
			return dat;
		}

		public void setDat(String dat) {
			this.dat = dat;
		}

		public FileNameFilterCustom(String dat) {
			this.dat = dat;
		}

		@Override
		public boolean accept(File dir, String fileName) {

			try {

				String suffix = fileName
						.substring(fileName.lastIndexOf(".") + 1);
				if (suffix != null && suffix.equalsIgnoreCase(this.getDat())) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

}
