# cheryl
Solving Cheryl's Birthday with clojure.core.logic

This project was largely inspired by John Feminella's [Solving the "Cheryl's Birthday" Problem with Prolog ](http://jxf.me/entries/cheryls-birthday/). If you don't know this puzzle, that post has a good description at the top. This writeup assumes you know the basics of Clojure (or any Lisp) but nothing about clojure.core.logic.

In core.logic (and other declarative languages like Prolog), we say what the answer looks like, and the engine finds it for us. This is in contrast with the imperative way, where we search for the answer ourself. So for a quick example:

```clojure
(run* [q]
  (membero q [1 2 3 4 5 6 7 8])
  (pred q even?)
  (conde
    [(== q 2) (== q 8)]
    [(== q 4)]
    [(== q 5)]
    [(== q 6)]
    [(== q 10)]))
; (4 6)
```
In English, find all (`run*`) `q`s such that `q` is in the list `[1 2 3 4 5 6 7 8]`, `q` is even, and `q` is either 2 and 8 at the SAME TIME, or 4, or 5, or 6, or 10. The only numbers that satisfy all the criteria are 4 and 6. 

Great! Now let's tackle the problem.
First, let's define all of Cheryl's possible birthdays.

```clojure
(def all-days [[:May 15] [:May 16] [:May 19] [:June 17] [:June 18] [:July 14]
               [:July 16] [:August 14] [:August 15] [:August 17]])
```

I suppose this strategy ensures a constant supply of presents throughout the summer for Cheryl. Now, we need to translate the dialogue into code.

> Albert: “I don’t know when your birthday is, but I know Bernard doesn’t know, either.”

Remember, Albert was only told Cheryl's birthday's month, and Bernard Cheryl's birthday's date. So think about how Albert *would* know Cheryl's birthday - this would only happen if there was a month in the list with only one date, and that was Cheryl's birthday month. By inspection it's obvious none of the months have only one day, but let's do this anyway, for generality.

We want to say that Cheryl's birthday month does not have one corresponding day - we can do this with a few helper functions and the `pred` macro. First, let's get all the days for a specific month `m`. Clojure's `group-by` function is great for problems like these, and we'll operate on the `first` element of each entry, the month.

```clojure
(defn days-for-month [days m]
  (get (group-by first days) m))
```

When we give this `all-days` and `:May`, for example, it returns `[[:May 15] [:May 16] [:May 19]]`. Now we just have to ensure that there isn't only one element in this list - otherwise, Albert would know Cheryl's birthday immediately. I'll define the opposite for clarity.

```clojure
(defn unique-day-for-month? [[days m]] 
  (= 1 (count (days-for-month days m))))`
```

Second, Albert knows that Bernard doesn't know Cheryl's birthday either. Bernard would only know her birthday if she gave him a date that only had one month corresponding to it. But remember that Albert only knows the month, so his statement is even more powerful: he means that if a month's list of days contains a unique day (like 19), Cheryl's birthday isn't in that month at all. In the following function, assume the functions `unique-month-for-day?` and `days-for-month` function like the similar ones above.

```clojure
(defn some-unique-days? [[days m]] 
  (boolean 
    (some
      (fn [[month d]] 
        (unique-month-for-day? [days d])) 
      (days-for-month days m))))
```

So this function asks, out of all the days for month, is there any such that there's only one month for that day? If so, return true. Side note: this function takes in a single argument because `pred` only allows us to pass one argument for some reason. Now let's use core.logic to search for all possibilities that are consistent after the first line of dialogue happens.

```clojure
(defn first-rule [days]
  (run* [m d]
        (membero [m d] days)
        (pred [days m] (complement unique-day-for-month?))
        (pred [days m] (complement some-unique-days?))))
```

Let `m` be Cheryl's birthday month and `d` the day. Now we can reason about them even though we don't actually know what they are yet. So lets say we did know we had the right `m` and `d`. First, we know that it's in the list Cheryl gave us - this is what the `membero` clause does. Then, we know that there's at least 2 days for that month - this corresponds to the first `pred` (notice we use `complement` to get the opposite of the predicate). Finally, we know that the month has no days that are unique to that month; this is expressed in the second `pred`. Running this on `all-days` makes core.logic search for all possibile days that match up with what we said, and it returns

```clojure
=> (first-rule all-days)
([:July 14] [:July 16] [:August 14] [:August 15] [:August 17])
```

Awesome. The rest of this should be straightforward, we did all the hard work for the first rule. Note that we can't just add in the other statements into the first `run*` statement - these are separate assertions. Albert can't not know and know Cheryl's birthday at the same time - and a `run*` is like a snapshot. So we need to separate each line of dialogue out, and feed the results of the previous one into the next one. Logicians use *dynamic epistemology* to analyze this type of situation - if you're interested, you can look up the Muddy Children problem (easier) and [xkcd's take](https://www.xkcd.com/blue_eyes.html) (harder).

> Bernard: “I didn’t know originally, but now I do.”

If Bernard knows Cheryl's birthday, then there must be only one month corresponding to the day he already knows, `d`.

```clojure
(defn second-rule [days]
  (run* [m d]
        (membero [m d] days)
        (pred [days d] unique-month-for-day?)))
```

Finally,
> Albert: “Well, now I know, too!”

If Albert knows Cheryl's birthday, then there must be only one day corresponding to the month he already knows, `m`.

```clojure
(defn third-rule [days]
  (run* [m d]
        (membero [m d] days)
        (pred [days m] unique-day-for-month?)))
```

That wasn't so bad. Putting it all together, we should come up with Cheryl's birthday! Let's try it out.

```clojure
(defn solve []
  (-> all-days
      first-rule
      second-rule
      third-rule))
=> (solve)
([:July 16])
```

The only possible date that matches all the criteria is July 16.
By now, Cheryl, Albert, and Bernard have become best friends - not to mention, top-notch logicians. 

I hope you enjoyed! Let me know at [rajk@berkeley.edu](rajk@berkeley.edu) or the issues page if you have any suggestions or feedback.
