/*
 * Copyright 2013 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.com.xinli.android.doudian.ui.widget.HorizontalListView;

import java.util.ArrayList;
import java.util.Collection;

import android.widget.BaseAdapter;

/**
 * A true ArrayList adapter providing access to all ArrayList methods.
 */
public abstract class ArrayAdapter<T> extends BaseAdapter {

	private ArrayList<T> mItems;

	/**
	 * Creates a new ArrayAdapter with an empty list.
	 */
	public ArrayAdapter() {
		this(null);
	}

	/**
	 * Creates a new ArrayAdapter with the specified list, or an empty list if
	 * items == null.
	 */
	public ArrayAdapter(ArrayList<T> items) {
		if (items != null) {
			mItems = items;
		} else {
			mItems = new ArrayList<T>();
		}
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public T getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Appends the specified element to the end of the list.
	 */
	public void add(T item) {
		mItems.add(item);
		notifyDataSetChanged();
	}

	/**
	 * Inserts the specified element at the specified position in the list.
	 */
	public void add(int index, T item) {
		mItems.add(index, item);
		notifyDataSetChanged();
	}

	/**
	 * Appends all of the elements in the specified collection to the end of the
	 * list, in the order that they are returned by the specified collection's
	 * Iterator.
	 */
	public void addAll(Collection<? extends T> items) {
		mItems.addAll(items);
		notifyDataSetChanged();
	}

	/**
	 * Appends all of the elements to the end of the list, in the order that
	 * they are specified.
	 */
	public void addAll(T... items) {
		for (T item : items) {
			mItems.add(item);
		}
		notifyDataSetChanged();
	}

	/**
	 * Inserts all of the elements in the specified collection into the list,
	 * starting at the specified position.
	 */
	public void addAll(int index, Collection<? extends T> items) {
		mItems.addAll(index, items);
		notifyDataSetChanged();
	}

	/**
	 * Inserts all of the elements into the list, starting at the specified
	 * position.
	 */
	public void addAll(int index, T... items) {
		for (int i = index; i < (items.length + index); i++) {
			mItems.add(i, items[i]);
		}
		notifyDataSetChanged();
	}

	/**
	 * Removes all of the elements from the list.
	 */
	public void clear() {
		mItems.clear();
		notifyDataSetChanged();
	}

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element.
	 */
	public void set(int index, T item) {
		mItems.set(index, item);
		notifyDataSetChanged();
	}

	/**
	 * Removes the specified element from the list
	 */
	public void remove(T item) {
		mItems.remove(item);
		notifyDataSetChanged();
	}

	/**
	 * Removes the element at the specified position in the list
	 */
	public void remove(int index) {
		mItems.remove(index);
		notifyDataSetChanged();
	}

	/**
	 * Removes all of the list's elements that are also contained in the
	 * specified collection
	 */
	public void removeAll(Collection<T> items) {
		mItems.removeAll(items);
		notifyDataSetChanged();
	}

	/**
	 * Retains only the elements in the list that are contained in the specified
	 * collection
	 */
	public void retainAll(Collection<T> items) {
		mItems.retainAll(items);
		notifyDataSetChanged();
	}

}