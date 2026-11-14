import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

/** Temporary body for screens that are not built yet. */
export function ScreenPlaceholder({ title, description }: { title: string; description: string }) {
  return (
    <Card className="max-w-3xl">
      <CardHeader>
        <CardTitle className="font-heading text-2xl">{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent className="text-sm text-muted-foreground">Not built yet: see docs/FRONTEND.md §2.</CardContent>
    </Card>
  );
}
