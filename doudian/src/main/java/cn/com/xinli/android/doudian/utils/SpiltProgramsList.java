package cn.com.xinli.android.doudian.utils;

import java.util.*;

public class SpiltProgramsList {
	// 游标初始指向中间
	private int cursorIndex;
	/** 循环截取集合开始 */
	private int view_size;// 页面一屏显示9个
	// 原始数据
	private ArrayList<Object> listRaw;
	// 原始集合尺寸/2
	private int halfIndex;
	// 显示数据
	private ArrayList<Object> listView;

	public SpiltProgramsList(ArrayList<?> listRaw, int halfIndex,
			int cursorIndex, int view_size) {
		this.listRaw = (ArrayList<Object>) listRaw;
		this.halfIndex = halfIndex;
		this.cursorIndex = cursorIndex;
		this.view_size = view_size;
		if (listView == null)
			listView = new ArrayList<Object>();
		System.out.println("cursor==" + this.cursorIndex);
	}

	public ArrayList<Object> initViewList() {
		// 模拟向右移动一次
		if (cursorIndex > 0)
			cursorIndex--;
		else
			cursorIndex = listRaw.size() - 1;

		if (cursorIndex < listRaw.size() - 1)
			cursorIndex++;
		else
			cursorIndex = 0;
		refreshViewList("right");

		for (int i = 0; i < listView.size(); i++) {
			System.out.println("listView.get(i)======" + listView.get(i));
		}
		return listView;
	}

	public ArrayList<Object> rightButtonClick() {
		if (cursorIndex < listRaw.size() - 1)
			cursorIndex++;
		else
			cursorIndex = 0;
		refreshViewList("right");

		for (int i = 0; i < listView.size(); i++) {
			System.out.println("listView.get(i)======" + listView.get(i));
		}
		return listView;
	}

	public ArrayList<Object> leftButtonClick() {
		if (cursorIndex > 0)
			cursorIndex--;
		else
			cursorIndex = listRaw.size() - 1;
		refreshViewList("left");
		for (int i = 0; i < listView.size(); i++) {
			System.out.println("listView.get(i)======" + listView.get(i));
		}
		return listView;
	}

	private ArrayList<Object> refreshViewList(String moveDirection) {
		System.out.println("refreshViewList===cursorIndex==" + cursorIndex);
		listView.clear();
		addLeftObject(cursorIndex, listRaw, listView, moveDirection);
		listView.add((Object) listRaw.get(cursorIndex));
		for (int i = 0; i < listView.size(); i++) {
			System.out.println("refreshViewList====listView.get(i)======"
					+ listView.get(i));
		}
		addRightObject(cursorIndex, listRaw, listView, moveDirection);
		return listView;
	}

	/**
	 * 获取游标对象左边的对象集合
	 * 
	 * @param cursor
	 * @param listRaw
	 * @return
	 */
	private void addLeftObject(int cursor, ArrayList<Object> listRaw,
			ArrayList<Object> listView, String moveDirection) {
		System.out.println("addLeftObject===halfIndex - cursorIndex=="
				+ (halfIndex - cursorIndex));
		if (halfIndex - cursorIndex < 0) {// 不需要递补
			System.out.println("addLeftObject===不需要递补==");
			int j = halfIndex;

			for (int i = 0; i < halfIndex; i++) {
				System.out.println("addLeftObject===不需要递补=j=" + j
						+ "===cursorIndex===" + cursorIndex);
				listView.add((Object) listRaw.get(cursorIndex - j));
				j--;
			}
		} else {
			for (int i = (listRaw.size() - (halfIndex - cursorIndex)); i < listRaw
					.size(); i++) {
				listView.add((Object) listRaw.get(i));

			}
			for (int i = 0; i < cursorIndex; i++) {
				listView.add((Object) listRaw.get(i));

			}
		}
		for (int i = 0; i < listView.size(); i++) {
			System.out.println("addLeftObject====listView.get(i)======"
					+ listView.get(i));
		}
	}

	/**
	 * 获取游标对象右边的对象集合
	 * 
	 * @param cursor
	 * @param listRaw
	 * @param moveDirection
	 *            移动方向
	 * @return
	 */
	private void addRightObject(int cursorIndex, ArrayList<Object> listRaw,
			ArrayList<Object> listView, String moveDirection) {
		for (int i = cursorIndex + 1; i <= cursorIndex + halfIndex; i++) {
			System.out.println("addRightObject====i=" + i);
			if (i >= listRaw.size())
				break;
			listView.add((Object) listRaw.get(i));
		}
		for (int i = 0; i < listView.size(); i++) {
			System.out.println("addRightObject====listView.get(i)=step111====="
					+ listView.get(i));
		}
		if (listView.size() < view_size) {// 需要递补
			System.out.println("addRightObject===需要递补====="
					+ (view_size - listView.size()));
			int fill = view_size - listView.size();
			for (int i = 0; i < fill; i++) {
				System.out.println("addRightObject===需要递补==i=====" + i);

				listView.add((Object) listRaw.get(i));
			}
		}
		for (int i = 0; i < listView.size(); i++) {
			System.out.println("addRightObject====listView.get(i)======"
					+ listView.get(i));
		}
	}
}
