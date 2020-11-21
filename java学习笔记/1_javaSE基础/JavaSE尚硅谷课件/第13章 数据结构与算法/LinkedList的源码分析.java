public class LinkedList<E>{
	int size = 0;
	Node<E> first;//头结点
	Node<E> last;//尾结点
	
	private static class Node<E> {
        E item; //数据项
        Node<E> next; //下一个结点的地址
        Node<E> prev; //上一个结点的地址

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
	
	public boolean add(E e) {
        linkLast(e);//连接到最后
        return true;
    }
	
	void linkLast(E e) {
		//last是当前链表的最后一个结点
        final Node<E> l = last;
		
		//创建新结点
		//Node(Node<E> prev, E element, Node<E> next) ：
		//新结点的prev是 原来链表的最后一个结点l，即让新结点的prev指向原来的最后一个结点
		//新结点的next是 null
		//新结点的数据项是：e
        final Node<E> newNode = new Node<>(l, e, null);
		
		//新结点变成了链表的最后一个结点
        last = newNode;
		
		//l是原来链表的最后一个结点
		//如果l==null，说明原来链表是一个空链表
        if (l == null)
			//first：链表的第一个结点，
			//现在这个新结点即是第一个结点，又是最后一个结点
            first = newNode;
        else//l==null不成立，说明原来的链表非空
			//l是原来链表的最后一个结点，原来的最后一个结点指向了新结点
            l.next = newNode;

		//结点个数增加
		size++;
        modCount++;
    }
	
	
	public void add(int index, E element) {
        checkPositionIndex(index);//检查index是否合法，合法的位置[0，size]

        if (index == size)//连接到最后
            linkLast(element);
        else //否则，就是插入到中间
            linkBefore(element, node(index));
    }
	
	private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }
	
	//返回了链表的[index]位置的结点
	Node<E> node(int index) {
        // assert isElementIndex(index);
		//index < (size >>1) 即  index < size/2，说明位置在链表的前半段
        if (index < (size >> 1)) {
			//从头开始找比较合适
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {//否则，说明位置在链表的后半段
			//从最后往前找比较合适
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }
	
	//e：是要新增的结点的数据
	//succ是[index]位置的结点
	void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
		//pred是[index]位置的前一个结点
        final Node<E> pred = succ.prev;
		//创建新结点，Node(Node<E> prev, E element, Node<E> next) 
		//新结点的prev指向[index]位置的前一个结点
		//新结点的next指向原来[index]的结点
        final Node<E> newNode = new Node<>(pred, e, succ);
		
		//原来[index]位置的结点  的prev指向新结点
        succ.prev = newNode;
		
		//原来[index]位置的前一个结点是null，说明要插入的位置是头结点的位置
        if (pred == null)
			//新结点就是新的头结点
            first = newNode;
        else
			//否则原来[index]位置的前一个结点的next指向新结点
            pred.next = newNode;
			
        size++;
        modCount++;
    }
	
	
	public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {//如果条件满足，说明x是要被删除的结点
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {//如果条件满足，说明x是要被删除的结点
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }
	
	//x是要被删除的结点
	E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;//被删除结点的数据
        final Node<E> next = x.next;//被删除结点的下一个结点
        final Node<E> prev = x.prev;//被删除结点的上一个结点

		//被删除结点的上一个结点是null，说明被删除结点是第一个结点
        if (prev == null) {
			//第一个结点变为被删除结点的下一个结点
            first = next;
        } else {
			//否则被删除结点的上一个结点 的next 指向 被删除结点的下一个结点
            prev.next = next;
			//被删除结点与它之前的上一个结点断开
            x.prev = null;
        }

		//被删除结点的下一个结点是null，说明被删除结点是最后一个结点
        if (next == null) {
			//链表的最后一个结点，变成被删除结点的上一个结点
            last = prev;
        } else {
			//否则，被删除结点的下一个结点的prev = 被删除结点的上一个结点
            next.prev = prev;
			//被删除结点与它之前的下一个结点断开
            x.next = null;
        }

		//把被删除结点的数据置空
        x.item = null;//经过x.prev = null ,x.next == null, x.item==null，相当于x与其结点断开关系，数据也置空，可以彻底被回收
        
		//元素个数减少
		size--;
        modCount++;
        return element;
    }
	
	public E remove(int index) {
        checkElementIndex(index);//检查index是否合法，合法的位置[0,size-1]
        return unlink(node(index));
    }
	
	private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	
	public E get(int index) {
        checkElementIndex(index);
		//node()返回了链表的[index]位置的结点
        return node(index).item;
    }
}