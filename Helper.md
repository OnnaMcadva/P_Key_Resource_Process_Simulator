## **📊 Java Collections Cheat Sheet**

| **Collection** | **Interface** | **Uniqueness** | **Order** | **Null Allowed** | **Thread-Safe** | **When to Use** |
|----------------|---------------|----------------|-----------|------------------|-----------------|------------------|
| **`ArrayList`** | `List` | No | Insertion order | Yes | No | Fast random access, frequent reads |
| **`LinkedList`** | `List` | No | Insertion order | Yes | No | Frequent insertions/deletions in middle |
| **`HashSet`** | `Set` | Yes | No order | One null | No | Fast uniqueness check, order not important |
| **`LinkedHashSet`** | `Set` | Yes | Insertion order | One null | No | Uniqueness + insertion order |
| **`TreeSet`** | `Set` | Yes | Sorted (natural/comparator) | No | No | Automatically sorted elements |
| **`HashMap`** | `Map` | Unique keys | No order | 1 null key, many null values | No | Fast key-value lookups |
| **`LinkedHashMap`** | `Map` | Unique keys | Insertion/access order | 1 null key | No | Map with predictable iteration order |
| **`TreeMap`** | `Map` | Unique keys | Sorted by keys | No null keys | No | Automatically sorted by keys |
| **`Vector`** | `List` | No | Insertion order | Yes | **Yes** | Legacy, use `CopyOnWriteArrayList` instead |
| **`Stack`** | `List` | No | LIFO | Yes | **Yes** | Stack data structure (LIFO) |
| **`ArrayDeque`** | `Deque` | No | Double-ended | No | No | Better stack/queue than `Stack` or `LinkedList` |
| **`PriorityQueue`** | `Queue` | No | Priority order | No | No | Priority-based processing |

## **🚀 Thread-Safe Alternatives**
| **Non-thread-safe** | **Thread-safe Equivalent** |
|----------------------|----------------------------|
| `ArrayList` | `CopyOnWriteArrayList` |
| `HashMap` | `ConcurrentHashMap` |
| `HashSet` | `Collections.synchronizedSet()` |
| `TreeMap` | `ConcurrentSkipListMap` |

## **⚡ Performance (Big O)**
| **Operation** | `ArrayList` | `LinkedList` | `HashSet`/`HashMap` | `TreeSet`/`TreeMap` |
|---------------|-------------|--------------|---------------------|---------------------|
| **Access** | O(1) | O(n) | O(1) average | O(log n) |
| **Insert** | O(n) | O(1) | O(1) average | O(log n) |
| **Delete** | O(n) | O(1) | O(1) average | O(log n) |
| **Search** | O(n) | O(n) | O(1) average | O(log n) |

## **🎯 Quick Selection Guide**
- **Need a list?** → `ArrayList` (usually), `LinkedList` (frequent modifications)
- **Need uniqueness?** → `HashSet` (usually), `TreeSet` (sorted)
- **Need key-value pairs?** → `HashMap` (usually), `TreeMap` (sorted)
- **Need thread-safety?** → `ConcurrentHashMap`, `CopyOnWriteArrayList`
- **Need queue/stack?** → `ArrayDeque` (best choice)
- **Need sorting?** → `TreeSet`/`TreeMap` or use `Collections.sort()`

## **💡 Pro Tips**
1. **`ArrayList` vs `LinkedList`**: Use `ArrayList` unless you need frequent add/remove in middle
2. **`HashSet` vs `TreeSet`**: Use `HashSet` unless you need sorted iteration
3. **`HashMap` vs `TreeMap`**: Use `HashMap` unless you need sorted keys
4. **`ArrayDeque`** is better than `Stack` for stack operations
5. For thread-safety, prefer `java.util.concurrent` collections over `Collections.synchronizedX()`




## **📊 Сравнение Java контейнеров и коллекций**

| **Контейнер** | **Интерфейс** | **Уникальность** | **Порядок** | **Null элементы** | **Потокобезопасность** | **Когда использовать** |
|---------------|--------------|------------------|-------------|-------------------|------------------------|------------------------|
| **`ArrayList`** | `List` | Нет | Сохраняет вставку | Да | Нет | Быстрый доступ по индексу, частые чтения |
| **`LinkedList`** | `List` | Нет | Сохраняет вставку | Да | Нет | Частые вставки/удаления в середине |
| **`HashSet`** | `Set` | Да | Нет | Однократный null | Нет | Проверка уникальности, не важен порядок |
| **`LinkedHashSet`** | `Set` | Да | Сохраняет вставку | Однократный null | Нет | Уникальность + порядок вставки |
| **`TreeSet`** | `Set` | Да | Сортированный | Нет | Нет | Автоматическая сортировка элементов |
| **`HashMap`** | `Map` | Уникальные ключи | Нет | 1 null ключ, много null значений | Нет | Быстрый поиск по ключу |
| **`LinkedHashMap`** | `Map` | Уникальные ключи | Сохраняет вставку | 1 null ключ | Нет | Map с порядком вставки |
| **`TreeMap`** | `Map` | Уникальные ключи | Сортированный по ключам | Нет (ключи) | Нет | Автоматическая сортировка по ключам |
| **`Vector`** | `List` | Нет | Сохраняет вставку | Да | **Да** | Устаревший, используй `CopyOnWriteArrayList` |
| **`Stack`** | `List` | Нет | LIFO | Да | **Да** | Структура стека (LIFO) |
| **`Queue`** (LinkedList) | `Queue` | Нет | FIFO | Да | Нет | Очередь (FIFO) |
| **`PriorityQueue`** | `Queue` | Нет | Приоритетная | Нет | Нет | Элементы с приоритетом |
| **`ArrayDeque`** | `Deque` | Нет | Двусторонняя | Нет | Нет | Стек или очередь (лучше чем Stack) |

## **⚡ Быстрый выбор:**
- **Нужен список?** → `ArrayList` (обычно), `LinkedList` (частые изменения)
- **Нужна уникальность?** → `HashSet` (обычно), `TreeSet` (сортировка)
- **Нужен словарь?** → `HashMap` (обычно), `TreeMap` (сортировка)
- **Потокобезопасность?** → `ConcurrentHashMap`, `CopyOnWriteArrayList`
- **Очередь/стек?** → `ArrayDeque` (лучший выбор)

## **🎯 Производительность (O-нотация):**
- **ArrayList**: доступ O(1), вставка O(n)
- **LinkedList**: доступ O(n), вставка O(1)
- **HashSet/HashMap**: поиск O(1) в среднем
- **TreeSet/TreeMap**: операции O(log n)

---

## Пакетное тестирование сценариев KRPSim

- Windows PowerShell:
	- Запуск всех сценариев (plain, 0/1/2 и проверка):
		- `scripts/run_all.ps1 [-Steps 200]`
	- Визуализация каждого (по очереди, нужно закрывать окно):
		- `scripts/visualize_all.ps1 [-Steps 100]`

- Linux/WSL (bash):
	- Запуск всех сценариев (plain, 0/1/2 и проверка):
		- `STEPS=200 scripts/run_all.sh`
	- Визуализация каждого (по очереди, с паузой):
		- `STEPS=100 scripts/visualize_all.sh`

Результаты сохраняются в папке `out/`:
- логи stderr: `out/logs/*.stderr.txt`
- трассы (stdout): `out/traces/*.trace`
- отчёты верификации: `out/verify/*.verify.txt`

